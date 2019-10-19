package com.scurab.uitor.common.util

inline fun <T> Collection<T>.forEachReversed(action: (T) -> Unit): Unit {
    for (i in size - 1 downTo 0) action(this.elementAt(i))
}

fun <KIN, VIN, KOUT, VOUT> Map<KIN, VIN>.map(keyMapper: (KIN) -> KOUT, valueMapper: (VIN) -> VOUT): Map<KOUT, VOUT> {
    return mapTo(keyMapper, valueMapper, mutableMapOf())
}

fun <KIN, VIN, KOUT, VOUT> Map<KIN, VIN>.mapTo(
    keyMapper: (KIN) -> KOUT,
    valueMapper: (VIN) -> VOUT,
    to: MutableMap<KOUT, VOUT>
): Map<KOUT, VOUT> {
    this.forEach { (k, v) ->
        to[keyMapper(k)] = valueMapper(v)
    }
    return to
}