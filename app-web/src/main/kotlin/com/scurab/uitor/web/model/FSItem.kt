package com.scurab.uitor.web.model

import com.scurab.uitor.common.util.ise
import kotlin.js.Json

class FSItem(private val json: Json) {
    val name = json.get("Name") as? String ?: ise("Missing field 'Name'")
    val size = json.get("Size") as? String ?: ise("Missing field 'Size'")
    val type = json.get("Type") as? Int ?: ise("Missing field 'Type'")
}