package com.scurab.dev.server

import java.io.File

fun main() {
    DevServer(
        arrayOf(
            File("app-web/src"),
            File("common/src/commonMain")
        ),
        "gradlew.bat app-web:_createIndexHtml",
        "127.0.0.1:8081"
    ).start()
}