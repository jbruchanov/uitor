package com.scurab.uitor.common.util

inline fun <T> Collection<T>.forEachReversed(action: (T) -> Unit): Unit {
    for (i in size - 1 downTo 0) action(this.elementAt(i))
}