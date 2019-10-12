package com.scurab.uitor.common.util

inline fun <reified T> arrayOf(size: Int, provider: (Int) -> T): Array<T> {
    val list = mutableListOf<T>()
    (0 until size).forEach {
        list.add(provider(it))
    }
    return list.toTypedArray()
}