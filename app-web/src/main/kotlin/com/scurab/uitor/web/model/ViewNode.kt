@file:Suppress("MemberVisibilityCanBePrivate")

package com.scurab.uitor.web.model

import com.scurab.uitor.common.model.IViewNode
import com.scurab.uitor.common.model.ViewNodeFields
import com.scurab.uitor.common.render.Rect
import com.scurab.uitor.common.util.dlog
import com.scurab.uitor.common.util.forEachReversed
import com.scurab.uitor.common.util.usingKey
import com.scurab.uitor.web.util.getMap
import com.scurab.uitor.web.util.getTypedListOf
import com.scurab.uitor.web.util.jsonField
import com.scurab.uitor.web.util.optJsonField
import d3.ITreeItem
import kotlin.js.Json

class ViewNode(json: Json) : IViewNode, ITreeItem {

    override val idi: Int by jsonField(json, ViewNodeFields.IDi)
    override val ids: String? by optJsonField(json, ViewNodeFields.IDs)
    override val level: Int by jsonField(json, ViewNodeFields.Level)
    override val position: Int by jsonField(json, ViewNodeFields.Position)
    override val owner: String by jsonField(json, ViewNodeFields.Owner)

    val rawdata: Map<String, Any?> = json.getMap(ViewNodeFields.Data)
    override val data: Map<String, Any?> = rawdata
        .toMutableMap()
        .apply {
            //TODO do this on server
            this[ViewNodeFields.IDi] = idi
            this[ViewNodeFields.IDs] = ids
            this[ViewNodeFields.Level] = level
            this[ViewNodeFields.Position] = position
            this[ViewNodeFields.Owner] = owner
        }
        .filter { !it.key.startsWith("_") }

    val dataSortedKeys = data.keys.sortedWith(COMPARATOR)

    override val nodes: List<ViewNode> = json.getTypedListOf(ViewNodeFields.Nodes) {
        try {
            ViewNode(it)
        } catch (e: Exception) {
            dlog { "Unable to create view node for position:${it.asDynamic().Position}, error:${e.message}" }
            throw e
        }
    }

    override val children: Array<ViewNode> = nodes.toTypedArray()

    override val rect: Rect by lazy {
        Rect(
            locationScreenX,
            locationScreenY,
            data.typed(ViewNodeFields.Width),
            data.typed(ViewNodeFields.Height)
        ).scaleSize(absoluteScaleX, absoluteScaleY)
    }
    val locationScreenX: Int by rawdata.usingKey(ViewNodeFields.LocationScreenX)
    val locationScreenY: Int by rawdata.usingKey(ViewNodeFields.LocationScreenY)
    val typeSimple: String by lazy { rawdata.typed<String>(ViewNodeFields.Type).substringAfterLast(".") }
    val typeAbbr: String by lazy { typeSimple.filter { it.toInt() in ('A'.toInt()..'Z'.toInt()) } }
    val type: String by rawdata.usingKey(ViewNodeFields.Type)
    val shouldRender: Boolean by rawdata.usingKey(ViewNodeFields.InternalRenderViewContent)
    val absoluteScaleX: Double by rawdata.usingKey(ViewNodeFields.InternalViewScaleX)
    val absoluteScaleY: Double by rawdata.usingKey(ViewNodeFields.InternalViewScaleY)
    val isLeaf: Boolean = children.isEmpty()
    val renderAreaRelative: Rect? by lazy {
        (rawdata[ViewNodeFields.InternalRenderAreaRelative] as? String)
            ?.split(",")
            ?.let { arrayOf(it[0].toInt(), it[1].toInt(), it[2].toInt(), it[3].toInt()) }
            ?.let { Rect(it[0], it[1], it[2] - it[0], it[3] - it[1]) }
    }

    override fun findFrontVisibleView(x: Int, y: Int, ignore: Set<IViewNode>): ViewNode? {
        //disabled for now, this makes views inactive actitivies "invisible" for search
        if (false && (data.typed<Int>(ViewNodeFields.InternalVisibility)) != 0) {//not visible
            return null;
        }

        if (rect.contains(x, y)) {
            //post-order
            nodes.forEachReversed { node ->
                val candidate = node.findFrontVisibleView(x, y, ignore)
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

    fun forEachIndexed(block: (Int, ViewNode) -> Unit) {
        block(position, this)
        nodes.forEach { it.forEachIndexed(block) }
    }

    fun all(): List<ViewNode> {
        val result = mutableListOf<ViewNode>()
        forEachIndexed { _, viewNode -> result.add(viewNode) }
        return result
    }

    private fun <T> Map<String, Any?>.typed(key: String): T {
        val v = this[key]
        check(containsKey(key)) { "Key:'${key}' doesn't exist in the map" }
        check(v != null) { "Value of Key:'${key}' is null" }
        return v as T
    }

    companion object {
        val COMPARATOR: Comparator<String> = object : Comparator<String> {
            private val orderMap = mapOf(
                // @formatter:off
                Pair(ViewNodeFields.Type,       "001"),
                Pair(ViewNodeFields.IDi,        "002"),
                Pair(ViewNodeFields.IDs,        "003"),
                Pair(ViewNodeFields.Level,      "004"),
                Pair(ViewNodeFields.Position,   "005"),
                Pair("Groovy Console",          "006"),
                Pair(ViewNodeFields.Owner,      "007"),
                Pair("Inheritance",             "008"),
                Pair("Context:",                "009"),
                Pair("StringValue",             "010")
                // @formatter:on
            )

            override fun compare(a: String, b: String): Int {
                val a = (orderMap[a] ?: "999") + a
                val b = (orderMap[b] ?: "999") + b
                return a.compareTo(b)
            }
        }
    }
}



