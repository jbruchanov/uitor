package com.scurab.uitor.web.model

import com.scurab.uitor.common.model.ConfigFields
import com.scurab.uitor.common.render.toColor
import com.scurab.uitor.common.util.map
import com.scurab.uitor.web.util.getMap
import com.scurab.uitor.web.util.jsonField
import kotlin.js.Json

class ClientConfig(private val json: Json) {
    val selectionColor by jsonField<String>(json, ConfigFields.SelectionColor)
    val serverVersion by jsonField<Int>(json, ConfigFields.ServerVersion)
    val device = json.getMap(ConfigFields.Device)
    val groovy by jsonField<Boolean>(json, ConfigFields.Groovy)
    val pointerIgnoreIds : Set<Int> = (json[ConfigFields.PointerIgnoreIds] as Array<Int>).toHashSet()

    val propertyHighlights =
        json.getMap(ConfigFields.PropertyHighlights)
            .map({ it.toRegex() }, { it.toString().toColor() })

    val typeHighlights = json.getMap(ConfigFields.TypeHighlights)
        .map({ it }, { it.toString().toColor() })
}