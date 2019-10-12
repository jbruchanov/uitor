package com.scurab.uitor.common.util

actual object Log {
    actual fun dlog(msg: String) {
        console.log(msg)
    }

    actual fun elog(msg: String) {
        console.error(msg)
    }
}