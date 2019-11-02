package com.scurab.uitor.web.model

import com.scurab.uitor.common.util.ise
import com.scurab.uitor.web.util.requireTypedListOf
import kotlin.js.Json

class ResourceItem(private val json: Json) {
    val dataType = json["DataType"].toString()
    val context = json["Context"] as? String
    val name = json["Name"].toString()
    val type = json["Type"].toString()
    var source : String? = null
    val id = json["id"] as? Int ?: ise("Missing 'id' field in Resources response")

    fun dataString() = json["Data"].toString()
    fun dataArrayPrimitives() = json["Data"] as Array<Any>
    fun dataArrayResourcesItems() = json.requireTypedListOf("Data") { ResourceItem(it) }
}