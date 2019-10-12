package com.scurab.uitor.common.util

actual object Log {
    actual fun dlog(msg: String) {
        System.out.println(msg)
    }

    actual fun elog(msg: String) {
        System.err.println(msg)
    }
}