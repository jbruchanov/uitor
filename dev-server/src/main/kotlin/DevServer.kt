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
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.slf4j.event.Level
import java.io.File
import java.nio.charset.Charset


class DevServer(
    private val watchingDirectories: Array<File>,
    private val rebuildCommand: String,
    private val localDeviceIp: String? = null
) {
    fun start() {
        val sockets = mutableSetOf<WebSocketServerSession>()
        FileWatcher().start(watchingDirectories) { path ->
            GlobalScope.launch {
                sockets.forEach {
                    println("Notify socket:${it.isActive}")
                    try {
                        it.send(Frame.Text("reload:${path.fileName}"))
                    } catch (e: Exception) {
                        println(e.message)
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
                    var file = File("app-web/index.html").readText()
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
                if(savingForDemo) {
                    val folder = File("c:/Temp/anuitor/${uri.substringBeforeLast("/", "")}")
                    folder.mkdirs()
                    val f = File("c:/Temp/anuitor/$uri")
                    f.writeBytes(result)
                }
                call.respond(result)
            } catch (e: ResponseException) {
                call.respond(e.response.status, e.response)
            } catch (e: Throwable) {
                call.respond(HttpStatusCode.InternalServerError, e.message ?: "Null exception message")
            }
        }

        //takes everything, even source code :(
        //get("/{...}", response)
        get("/screens", response)
        get("/screencomponents", response)
        get("/screenstructure", response)
        get("/config", response)
        get("/logcat/{type?}", response)
        get("/screen/{index}", response)
        get("/resources/all", response)
        get("/resources/list", response)
        get("/resources/{screenIndex?}/{resId?}", response)
        get("/storage", response)
        get("/storage?path={path}", response)
        get("/view/{screen}/{id}", response)
        get("/viewhierarchy/{screenIndex}", response)
        get("/view/{screen}/{id}/{property}/{reflection?}/{maxDepth?}/", response)

        post("/groovy") {
            val requestText = call.receiveText()
            val result = httpClient.post<String>("http://$localDeviceIp/groovy") {
                body = requestText
            }
            call.respond(result)
        }
    }

    private fun executeRebuildCommand(): Boolean {
        val p = ProcessBuilder(rebuildCommand.split(" "))
            .directory(File(File("").absolutePath))
            .start()

        val resultOutput = p.inputStream.readBytes().toString(Charset.defaultCharset())
        p.waitFor()
        val result = p.exitValue() == 0
        if (!result) {
            println(resultOutput)
        }
        return result
    }
}