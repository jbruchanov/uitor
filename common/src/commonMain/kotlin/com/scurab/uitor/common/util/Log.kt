package com.scurab.uitor.common.util

const val DEBUG = 3
const val VERBOSE = 2
const val INFO = 1
const val OFF = 0
const val LOGGING_MODE = VERBOSE

expect object Log {
    fun dlog(msg: String)
    fun elog(msg: String)
}

inline fun dlog(msg: () -> String) {
    println(msg())
}

inline fun dlog(tag: String = "", msg: () -> String) {
    if (LOGGING_MODE >= DEBUG) {
        println("[$tag]:${msg()}")
    }
}

inline fun vlog(tag: String = "", msg: () -> String) {
    if (LOGGING_MODE >= VERBOSE) {
        println("[$tag]:${msg()}")
    }
}

inline fun ilog(tag: String = "", msg: () -> String) {
    if (LOGGING_MODE >= INFO) {
        println("[$tag]:${msg()}")
    }
}

inline fun elog(tag: String = "", msg: () -> String) {
    println("[$tag]:${msg()}")
}