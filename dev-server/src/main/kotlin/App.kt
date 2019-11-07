package com.scurab.dev.server

import java.io.File

fun main() {
    val osName = System.getProperty("os.name")
    val gradle = when {
        osName.contains("windows", ignoreCase = true) -> "gradlew.bat"
        else -> "./gradlew"
    }
    DevServer(
        arrayOf(
            File("app-web/src"),
            File("common/src/commonMain")
        ),
        "$gradle app-web:createDevIndexHtml",
        "127.0.0.1:8081"
    ).start()
}