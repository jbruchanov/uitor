package com.scurab.uitor.web.model

import com.scurab.uitor.common.model.ConfigFields
import com.scurab.uitor.common.render.Color
import com.scurab.uitor.common.render.toColor
import com.scurab.uitor.common.util.map
import com.scurab.uitor.common.util.npe
import com.scurab.uitor.web.util.getMap
import com.scurab.uitor.web.util.jsonField
import kotlin.js.Json

interface IClientConfig {
    val selectionColor: String
    val serverVersion: Int
    val device: Map<String, Any?>
    val groovy: Boolean
    val pointerIgnoreIds: Set<Int>
    val pages: Array<String>
    val propertyHighlights: Map<Regex, Color>
    val typeHighlights: Map<String, Color>
    var detail: String?
    var snapshotResources: Boolean
    val deviceInfo: String
}

class ClientConfig(private val json: Json) : IClientConfig {
    override val selectionColor by jsonField<String>(json, ConfigFields.SelectionColor)
    override val serverVersion by jsonField<Int>(json, ConfigFields.ServerVersion)
    override val device = json.getMap(ConfigFields.Device)
    override val groovy by jsonField<Boolean>(json, ConfigFields.Groovy)
    override val pointerIgnoreIds: Set<Int> = (json[ConfigFields.PointerIgnoreIds] as? Array<Int> ?: emptyArray()).toHashSet()
    override val pages: Array<String> = json[PAGES] as? Array<String> ?: npe("Undefined field 'Pages'")

    override val propertyHighlights =
        json.getMap(ConfigFields.PropertyHighlights)
            .map({ it.toRegex() }, { it.toString().toColor() })

    override val typeHighlights = json.getMap(ConfigFields.TypeHighlights)
        .map({ it }, { it.toString().toColor() })

    override var detail: String? = json[DETAIL] as? String
    override var snapshotResources: Boolean = json[SNAPSHOT_RESOURCES] as? Boolean ?: false

    override val deviceInfo: String by lazy {
        val man = device["MANUFACTURER"]?.toString() ?: ""
        val model = device["MODEL"]?.toString() ?: ""
        val api = device["API"]?.toString()?.toIntOrNull()?.let { "API:$it" } ?: ""
        var item = arrayOf(man, model, api).filter { it.isNotEmpty() }.joinToString(" ")
        if (detail != null) {
            item = "$item\n$detail"
        }
        item
    }

    companion object {
        const val DETAIL = "detail"
        const val PAGES = "Pages"
        const val SNAPSHOT_RESOURCES = "SnapshotResources"
        //fallback if we can't load device config
        val FallBackConfig = object : IClientConfig {
            override val selectionColor: String = "#F00"
            override val serverVersion: Int = 0
            override val device: Map<String, Any?> = emptyMap()
            override val groovy: Boolean = false
            override val pointerIgnoreIds: Set<Int> = emptySet()
            //none, just have save button
            override val pages: Array<String> = emptyArray()
            override val propertyHighlights: Map<Regex, Color> = emptyMap()
            override val typeHighlights: Map<String, Color> = emptyMap()
            override var detail: String? = null
            override var snapshotResources: Boolean = false
            override val deviceInfo: String = ""
        }
    }
}

object Pages {
    const val LayoutInspector = "LayoutInspectorPage"
    const val ThreeD = "ThreeDPage"
    const val TidyTree = "TidyTreePage"
    const val Resources = "ResourcesPage"
    const val FileBrowser = "FileBrowserPage"
    const val Windows = "WindowsPage"
    const val WindowsDetailed = "WindowsDetailedPage"
    const val Screenshot = "ScreenshotPage"
    const val LogCat = "LogCatPage"
    const val Groovy = "GroovyPage"
}