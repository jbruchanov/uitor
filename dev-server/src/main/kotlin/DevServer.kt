package com.scurab.dev.server

import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.CallLogging
import io.ktor.features.DefaultHeaders
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.readText
import io.ktor.http.content.files
import io.ktor.http.content.static
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.websocket.WebSocketServerSession
import io.ktor.websocket.WebSockets
import io.ktor.websocket.webSocket
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import java.nio.charset.Charset


class DevServer(
    private val watchingDirectory: File,
    private val rebuildCommand: String
) {
    fun start() {
        val sockets = mutableSetOf<WebSocketServerSession>()
        FileWatcher().start(watchingDirectory) { path ->
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
            install(WebSockets)
            install(CallLogging)
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

                static("build") {
                    files(File("app-web/build"))
                }

                static {
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