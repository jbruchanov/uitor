package com.scurab.dev.server

import java.io.File

fun main() {
    DevServer(
        File("app-web/src/main"),
        "gradlew.bat app-web:_createIndexHtml"
    ).start()
}