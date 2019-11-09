package com.scurab.uitor.web.model

import com.scurab.uitor.common.util.npe
import com.scurab.uitor.web.util.getTypedListOf
import kotlin.js.Json

class ScreenNode(json: Json, val level: Int = 0) {
    val name: String = json["name"] as? String ?: npe("Missing 'name' field")
    val children: List<ScreenNode> = json.getTypedListOf("children") { ScreenNode(it, level + 1) }

    fun forEach(block: (ScreenNode) -> Unit) {
        block(this)
        children.forEach { it.forEach(block) }
    }
}