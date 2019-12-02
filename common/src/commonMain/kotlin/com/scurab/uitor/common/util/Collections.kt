package com.scurab.uitor.common.util

import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

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


fun <T> Map<String, Any?>.usingKey(
    key: String,
    default: (key: String) -> T = { throw IllegalArgumentException("Missing value for key:'${key}'") }
): MapDelegate<T> {
    return MapDelegate(key, this, default)
}

class MapDelegate<T>(
    private val key: String,
    private val map: Map<String, Any?>,
    private val default: (key: String) -> T
) : ReadOnlyProperty<Any, T> {
    override fun getValue(thisRef: Any, property: KProperty<*>): T {
        return if (!map.containsKey(key)) {
            default(key)
        } else {
            @Suppress("UNCHECKED_CAST")
            map[key] as T
        }
    }
}