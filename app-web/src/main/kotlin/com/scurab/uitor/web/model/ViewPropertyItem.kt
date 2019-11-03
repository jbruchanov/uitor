package com.scurab.uitor.web.model

import com.scurab.uitor.common.util.ise
import com.scurab.uitor.web.util.getMap
import kotlin.js.Json

class ViewPropertyItem(private val json: Json) : IResourceItem {
    override val context: String? = null
    val name = json["Name"]?.toString()
    override val dataType = json["DataType"] as String?
    val properties = json.getContext()

    override fun dataString(): String {
        return json["Data"] as? String ?: ise("Missing field 'Data'")
    }

    override fun dataArrayResourcesItems(): List<IResourceItem> {
        TODO("not implemented")
    }

    override fun dataArrayPrimitives(): Array<Any> {
        TODO("not implemented")
    }

    private fun Json.getContext(): Map<String, Any?> {
        val any = this["Context"]
        //null object, TODO handle this in better way on server
        return if (any is String) {
            emptyMap()
        } else {
            getMap("Context")
        }
    }
}