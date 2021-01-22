package com.scurab.dev.server

import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.features.ResponseException
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.features.CallLogging
import io.ktor.features.DefaultHeaders
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.readText
import io.ktor.http.content.files
import io.ktor.http.content.static
import io.ktor.request.receiveText
import io.ktor.request.uri
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.util.pipeline.PipelineInterceptor
import io.ktor.websocket.WebSocketServerSession
import io.ktor.websocket.WebSockets
import io.ktor.websocket.webSocket
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import org.slf4j.event.Level
import java.io.File
import java.nio.charset.Charset
import java.nio.file.Path


/**
 * Naive implementation for of dev server to provide proxy and notify client
 * if something change with potential rebuild on demand from "js"
 * Doesn't handle case of hammering of build
 */
class DevServer(
    private val watchingDirectories: Array<File>,
    private val rebuildCommand: String,
    private val localDeviceIp: String? = null
) {
    fun start() {
        val sockets = mutableSetOf<WebSocketServerSession>()
        val channel = Channel<Path>()
        FileWatcher().start(watchingDirectories) { path ->
            channel.offer(path)
        }
        channel
            .receiveAsFlow()
            .debounce(500)
            .let {
                GlobalScope.launch {
                    it.collect { latestPath ->
                        println("FileWatcher change $latestPath sockets:${sockets.size}")
                        sockets.forEach {
                            try {
                                it.send(Frame.Text("reload:${latestPath.fileName}"))
                            } catch (e: Exception) {
                                println(e.message)
                            }
                        }
                    }
                }
            }


        val port = 8080
        embeddedServer(Netty, port) {
            install(CallLogging) {
                level = Level.INFO
            }
            install(WebSockets)
            install(DefaultHeaders) {
                header("Access-Control-Allow-Origin", "*")
            }

            routing {
                get("/") {
                    var file = File("app-web/build/distributions/index.html").readText()
                    val reloadScript = File("dev-server/src/main/resources/websockets.js")
                        .readText()
                        .replace("%PORT%", port.toString())
                    file += "\n<script>$reloadScript</script>"
                    call.respondText(file, ContentType.parse("text/html"))
                }

                static("/common/src/commonMain/kotlin") {
                    files(File("common/src/commonMain/kotlin"))
                }

                static("build") {
                    files(File("app-web/build"))
                }

                if (localDeviceIp != null) {
                    initRemoteServerRoutes(localDeviceIp)
                }

                static {
                    files(File("app-web/src/test/resources/v1"))
                    files(File("app-web/src/main/resources"))
                    files(File("app-web"))
                    files(File("app-web/build/distributions/"))
                }

                webSocket("/wss/files") {
                    // websocketSession
                    try {
                        println("Connected:${this.call.request.local.remoteHost}")
                        println("Sockets:${sockets.size}")
                        sockets.add(this)
                        while (true) {
                            val msg = (incoming.receive() as? Frame.Text)?.readText()
                            if ("rebuild" == msg) {
                                val result = try {
                                    executeRebuildCommand()
                                } catch (e: Exception) {
                                    false
                                }
                                outgoing.send(Frame.Text(if (result) "reloaded" else "failed"))
                            }
                        }
                    } catch (e: Throwable) {
                        e.printStackTrace()
                        sockets.remove(this)
                    }
                    println("Disconnected:${this.call.request.local.remoteHost}")
                }
            }
        }.start(wait = true)
    }

    private val savingForDemo = false
    private fun Routing.initRemoteServerRoutes(localDeviceIp: String) {
        val httpClient = HttpClient(CIO)
        val response: PipelineInterceptor<Unit, ApplicationCall> = {
            val uri = call.request.uri.substringAfter("/")
            try {
                val result = httpClient.get<ByteArray>("http://$localDeviceIp/${uri}")
                if (savingForDemo) {
                    val folder = File("c:/Temp/anuitor/${uri.substringBeforeLast("/", "")}")
                    folder.mkdirs()
                    val f = File("c:/Temp/anuitor/$uri")
                    f.writeBytes(result)
                }
                call.respond(result)
            } catch (e: ResponseException) {
                call.respond(
                    e.response?.status ?: HttpStatusCode.InternalServerError,
                    e.response ?: "null"
                )
            } catch (e: Throwable) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    e.message ?: "Null exception message"
                )
            }
        }

        //takes everything, even source code :(
        //get("/{...}", response)
        get("/api/screens", response)
        get("/api/api/screencomponents", response)
        get("/api/screenstructure", response)
        get("/api/config", response)
        get("/api/logcat/{type?}", response)
        get("/api/screen/{index}", response)
        get("/api/resources/all", response)
        get("/api/resources/list", response)
        get("/api/resources/{screenIndex?}/{resId?}", response)
        get("/api/storage", response)
        get("/api/storage?path={path}", response)
        get("/api/view/{screen}/{id}", response)
        get("/api/viewhierarchy/{screenIndex}", response)
        get("/api/view/{screen}/{id}/{property}/{reflection?}/{maxDepth?}/", response)

        post("/api/groovy") {
            val requestText = call.receiveText()
            val result = httpClient.post<String>("http://$localDeviceIp/groovy") {
                body = requestText
            }
            call.respond(result)
        }
    }

    private fun executeRebuildCommand(): Boolean {
        println("Rebuild started")
        val p = ProcessBuilder(rebuildCommand.split(" "))
            .directory(File(File("").absolutePath))
            .start()

        val resultOutput = p.inputStream.readBytes().toString(Charset.defaultCharset())
        p.waitFor()
        val result = p.exitValue() == 0
        println("Rebuild finished with result:$result")
        if (!result) {
            println(resultOutput)
        }
        return result
    }
}