@file:Suppress("UNCHECKED_CAST", "UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")

package com.scurab.uitor.web.util

import com.scurab.uitor.common.util.elog
import kotlin.js.Json
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

@JsName("Object")
external object JsObject {
    fun keys(json: Json): Array<String>
}

/**
 * Get keys of js object
 */
fun Json.keys(): Array<String> = JsObject.keys(this)

fun <T> Json.opt(key: String): T? {
    return get(key) as? T
}

fun <T> Json.require(key: String): T {
    val v = get(key)
    check(v != null) { "Null value for key:'$key'" }
    return v as T
}

/**
 * Convert json array into typed array.
 * If the field is not present in the json object, empty list is returned
 * @param key
 * @param creator lamda to create an object from json
 */
fun <T> Json.getTypedListOf(key: String, creator: (Json) -> T): List<T> {
    try {
        return (get(key) as? Array<Json>)?.let { arr ->
            arr.map { item -> creator(item) }
        } ?: emptyList()
    } catch (e: Exception) {
        elog { "Exception '${e.message}' key:$key, object:$this" }
        throw e
    }
}

fun <T> Json.requireTypedListOf(key: String, creator: (Json) -> T): List<T> {
    val v = get(key)
    check(v != null) { "Null value for key:'$key'" }
    val array = v as Array<Json>
    return array.map { creator(it) }
}

fun Json.getMap(key: String?, to: MutableMap<String, Any?> = linkedMapOf()): MutableMap<String, Any?> {
    try {
        val obj = if (key == null) this else get(key) as? Json
        if (obj != null) {
            for (k in obj.keys().sorted()) {
                val value = obj[k]
                to[k] = value
            }
        }
        return to
    } catch (e: Exception) {
        elog { "Exception '${e.message}' key:$key, object:$this" }
        throw e
    }
}

fun Map<String, Any?>.toJson(): Json {
    val json = obj<Json>()
    this.forEach { (k, v) ->
        json[k] = v
    }
    return json
}

fun <T> jsonField(json: Json, name: String? = null): ReadOnlyProperty<Any, T> = JsonDelegate(json, name)

private class JsonDelegate<T>(private val json: Json, private val name: String?) : ReadOnlyProperty<Any, T> {
    override fun getValue(thisRef: Any, property: KProperty<*>): T {
        val propertyName = name ?: property.name
        return try {
            val v = json[propertyName]
            check(v != null) { "Null value for key:'$propertyName'" }
            v as T
        } catch (e: Exception) {
            elog { "Exception casting property:'$propertyName' on $json" }
            throw e
        }
    }
}

fun <T> optJsonField(json: Json, name: String? = null): ReadOnlyProperty<Any, T?> = OptJsonDelegate(json, name)

private class OptJsonDelegate<T>(private val json: Json, private val name: String?) : ReadOnlyProperty<Any, T?> {
    override fun getValue(thisRef: Any, property: KProperty<*>): T? {
        val propertyName = name ?: property.name
        return try {
            json[propertyName] as? T
        } catch (e: Exception) {
            elog { "Exception casting property:'$propertyName' on $json" }
            throw e
        }
    }
}
