package com.scurab.uitor.common.util

inline fun <reified T> arrayOf(size: Int, provider: (Int) -> T): Array<T> {
    val list = mutableListOf<T>()
    (0 until size).forEach {
        list.add(provider(it))
    }
    return list.toTypedArray()
}

inline fun <T> Collection<T>.forEachReversed(action: (T) -> Unit): Unit {
    for (i in size - 1 downTo 0) action(this.elementAt(i))
}