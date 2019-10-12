@file:Suppress("MemberVisibilityCanBePrivate")

package com.scurab.uitor.web.model

import com.scurab.uitor.common.model.IViewNode
import com.scurab.uitor.common.model.ViewNodeFields
import com.scurab.uitor.common.render.Rect
import com.scurab.uitor.common.util.dlog
import com.scurab.uitor.web.util.getMap
import com.scurab.uitor.web.util.getTypedListOf
import com.scurab.uitor.web.util.jsonField
import com.scurab.uitor.web.util.optJsonField
import kotlin.js.Json

class ViewNode(json: Json) : IViewNode {

    override val idi: Int by jsonField(json, ViewNodeFields.IDi)
    override val ids: String? by optJsonField(json, ViewNodeFields.IDs)
    override val level: Int by jsonField(json, ViewNodeFields.Level)
    override val position: Int by jsonField(json, ViewNodeFields.Position)
    override val owner: String by jsonField(json, ViewNodeFields.Owner)

    override val data: Map<String, Any?> = json.getMap(ViewNodeFields.Data)
    override val nodes: List<ViewNode> = json.getTypedListOf(ViewNodeFields.Nodes) {
        try {
            ViewNode(it)
        } catch (e: Exception) {
            dlog { "Unable to create view node for position:${it.asDynamic().Position}, error:${e.message}" }
            throw e
        }
    }

    override val rect: Rect by lazy {
        Rect(
            locationScreenX,
            locationScreenY,
            data.int(ViewNodeFields.Width),
            data.int(ViewNodeFields.Height)
        )
    }
    val locationScreenX: Int get() = data.int(ViewNodeFields.LocationScreenX)
    val locationScreenY: Int get() = data.int(ViewNodeFields.LocationScreenY)

    override fun findFrontVisibleView(x: Int, y: Int, ignore: Set<IViewNode>): ViewNode? {
        //disabled for now, this makes views inactive actitivies "invisible" for search
        if (false && (data.int(ViewNodeFields.InternalVisibility)) != 0) {//not visible
            return null;
        }

        if (rect.contains(x, y)) {
            //post-order
            (nodes.size - 1 downTo 0).forEach { i ->
                val candidate = nodes[i].findFrontVisibleView(x, y, ignore)
                if (candidate != null) {
                    return candidate
                }
            }
            if (ignore.contains(this)) {
                return null
            }
            return this
        }
        return null
    }

    private fun Map<String, Any?>.int(key: String): Int {
        try {
            return this[key] as Int
        } catch (e: Exception) {
            println("Unable to get Int of key:$key in $this")
            throw e
        }
    }
}