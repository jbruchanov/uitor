package com.scurab.uitor.web.model

import com.scurab.uitor.common.model.ConfigFields
import com.scurab.uitor.common.render.Color
import com.scurab.uitor.common.render.toColor
import com.scurab.uitor.web.util.getMap
import com.scurab.uitor.web.util.jsonField
import kotlin.js.Json

class ClientConfig(private val json: Json) {
    val selectionColor by jsonField<String>(json, ConfigFields.SelectionColor)
    val propertyHighlights = convertPropertyHighlights(json.getMap(ConfigFields.PropertyHighlights))
    val serverVersion by jsonField<Int>(json, ConfigFields.ServerVersion)
    val device = json.getMap(ConfigFields.Device)
    val typeHighlights = json.getMap(ConfigFields.TypeHighlights)
    val groovy by jsonField<Boolean>(json, ConfigFields.Groovy)
    val pointerIgnoreIds by jsonField<IntArray>(json, ConfigFields.PointerIgnoreIds)

    private fun convertPropertyHighlights(propertyHighlights: Map<String, *>): Map<Regex, Color> {
        val result = mutableMapOf<Regex, Color>()
        propertyHighlights.forEach { (regex, color) ->
            result[regex.toRegex()] = color.toString().toColor()
        }
        return result
    }
}