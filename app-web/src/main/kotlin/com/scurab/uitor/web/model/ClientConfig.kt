package com.scurab.uitor.web.model

import com.scurab.uitor.common.model.ConfigFields
import com.scurab.uitor.web.util.getMap
import com.scurab.uitor.web.util.jsonField
import kotlin.js.Json

class ClientConfig(private val json: Json) {
    val selectionColor by jsonField<String>(json, ConfigFields.SelectionColor)
    val propertyHighlights by json.getMap(ConfigFields.PropertyHighlights)
    val serverVersion by jsonField<Int>(json, ConfigFields.ServerVersion)
    val device by json.getMap(ConfigFields.Device)
    val typeHighlights: Map<String, String> by json.getMap(ConfigFields.TypeHighlights)
    val groovy by jsonField<Boolean>(json, ConfigFields.Groovy)
    val pointerIgnoreIds by jsonField<IntArray>(json, ConfigFields.PointerIgnoreIds)
}