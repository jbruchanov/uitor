@file:Suppress("MemberVisibilityCanBePrivate")

package com.scurab.uitor.web.model

import com.scurab.uitor.common.model.IViewNode
import com.scurab.uitor.common.model.ViewNodeFields
import com.scurab.uitor.common.render.Rect
import com.scurab.uitor.common.util.capitalLetters
import com.scurab.uitor.common.util.dlog
import com.scurab.uitor.common.util.forEachReversed
import com.scurab.uitor.common.util.usingKey
import com.scurab.uitor.web.ui.viewproperties.ViewPropertiesTableViewComponents.GROOVY_NAME
import com.scurab.uitor.web.util.getMap
import com.scurab.uitor.web.util.getTypedListOf
import com.scurab.uitor.web.util.jsonField
import com.scurab.uitor.web.util.optJsonField
import js.d3.ITreeItem
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
    val typeAbbr: String by lazy { typeSimple.capitalLetters() }
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

    override fun findFrontVisibleView(x: Int, y: Int, ignore: Set<Int>): ViewNode? {
        //disabled for now, this makes views inactive actitivies "invisible" for search
        if (false && (data.typed<Int>(ViewNodeFields.InternalVisibility)) != 0) {//not visible
            return null
        }

        if (rect.contains(x, y)) {
            //post-order
            nodes.forEachReversed { node ->
                val candidate = node.findFrontVisibleView(x, y, ignore)
                if (candidate != null) {
                    return candidate
                }
            }
            if (ignore.contains(idi) || ignore.contains(position)) {
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
        private val orderMap = mapOf(
            // @formatter:off
            Pair(ViewNodeFields.Type,       "001"),
            Pair(ViewNodeFields.IDi,        "002"),
            Pair(ViewNodeFields.IDs,        "003"),
            Pair(ViewNodeFields.Level,      "004"),
            Pair(ViewNodeFields.Position,   "005"),
            Pair(GROOVY_NAME,               "006"),
            Pair(ViewNodeFields.Owner,      "007"),
            Pair("Inheritance",             "008"),
            Pair("Context:",                "009"),
            Pair("Extractor",               "010"),
            Pair("StringValue",             "011")
            // @formatter:on
        )
        fun orderKey(value: String): String = (orderMap[value] ?: "999") + value

        val COMPARATOR: Comparator<String> = object : Comparator<String> {
            override fun compare(a: String, b: String): Int {
                return orderKey(a).compareTo(orderKey(b))
            }
        }
    }
}

/**
 * Return true if the set contains ID or Position of view
 */
internal fun ViewNode.isIgnored(ignoring: Set<Int>): Boolean {
    return ignoring.contains(idi) || ignoring.contains(position)
}

/**
 * Toggle value, add if missing, remove if exists
 * Returns new value
 */
internal fun ViewNode.toggleIgnored(ignoring: MutableSet<Int>): Boolean {
    val isIgnored = isIgnored(ignoring)
    //id is remove-only as it's passed only from client now
    if (isIgnored) {
        ignoring.remove(position)
        ignoring.remove(idi)
    } else {
        ignoring.add(position)
    }
    return !isIgnored
}