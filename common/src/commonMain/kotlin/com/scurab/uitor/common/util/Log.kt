package com.scurab.uitor.common.util

const val DEBUG = true

expect object Log {
    fun dlog(msg: String)
    fun elog(msg: String)
}

inline fun dlog(msg: () -> String) {
    println(msg())
}

inline fun dlog(tag: String = "", msg: () -> String) {
    if (DEBUG) {
        println("[$tag]:${msg()}")
    }
}

inline fun elog(tag: String = "", msg: () -> String) {
    if (DEBUG) {
        println("[$tag]:${msg()}")
    }
}