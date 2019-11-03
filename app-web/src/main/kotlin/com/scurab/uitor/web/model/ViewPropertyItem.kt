package com.scurab.uitor.web.model

import com.scurab.uitor.common.util.ise
import com.scurab.uitor.web.util.getMap
import kotlin.js.Json

class ViewPropertyItem(private val json: Json) : IResourceItem {
    override val context: String? = null
    val name = json["Name"]?.toString()
    override val dataType = json["DataType"] as String?
    val properties = json.getMap("Context")

    override fun dataString(): String {
        return json["Data"] as? String ?: ise("Missing field 'Data'")
    }

    override fun dataArrayResourcesItems(): List<IResourceItem> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun dataArrayPrimitives(): Array<Any> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}