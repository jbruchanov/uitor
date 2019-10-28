package com.scurab.uitor.common.util

fun npe(msg: String): Nothing = throw NullPointerException(msg)
fun ise(msg: String): Nothing = throw IllegalStateException(msg)

val <T> T?.ref: T
    get() {
        if (this == null) {
            throw NullPointerException("Reference is null, mostlikely invalid lifecycle usage")
        }
        return this
    }