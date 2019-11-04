package com.scurab.dev.server

import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.CallLogging
import io.ktor.features.DefaultHeaders
import io.ktor.http.ContentType
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.readText
import io.ktor.http.content.URIFileContent
import io.ktor.http.content.files
import io.ktor.http.content.static
import io.ktor.request.uri
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.Routing
import io.ktor.routing.get
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
import java.net.URL
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

    private fun Routing.initRemoteServerRoutes(localDeviceIp: String) {
        val response: PipelineInterceptor<Unit, ApplicationCall> = {
            var uri = call.request.uri.substringAfter("/")
            if (!uri.contains("screenIndex=")) {
                var s = "screenIndex=0"
                if (!uri.contains("?")) {
                    s = "?$s"
                } else {
                    s = "&$s"
                }
                uri += s
            }
            call.respond(URIFileContent(URL("http://$localDeviceIp/${uri}")))
        }

        get("/{data}.json", response)
        get("/screen.png", response)
        get("/view.png", response)
        get("/screencomponents.html", response)
        get("/logcat.txt", response)
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