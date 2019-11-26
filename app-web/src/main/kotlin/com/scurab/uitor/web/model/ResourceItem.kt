package com.scurab.uitor.web.model

import com.scurab.uitor.common.util.ise
import com.scurab.uitor.web.util.requireTypedListOf
import kotlin.js.Json

interface IResourceItem {
    val dataType: String?
    fun dataString(): String
    fun dataArrayResourcesItems(): List<IResourceItem>
    fun dataArrayPrimitives(): Array<Any>
    val context: String?
}

class ResourceItem(private val json: Json) : IResourceItem {
    override val dataType = json["DataType"].toString()
    override val context = json["Context"] as? String
    val name = json["Name"].toString()
    val type = json["Type"].toString()
    var source: String? = null
    val id = json["id"] as? Int ?: ise("Missing 'id' field in Resources response")

    override fun dataString() = json["Data"].toString()
    override fun dataArrayPrimitives() = json["Data"] as Array<Any>
    override fun dataArrayResourcesItems() = json.requireTypedListOf("Data") { ResourceItem(it) }
}

interface IResourceDTO {
    val key: Int
    val value: String
    val contextValue: String?
}

class ResourceDTO(
    override val key: Int,
    override val value: String,
    override val contextValue: String?
) : IResourceDTO