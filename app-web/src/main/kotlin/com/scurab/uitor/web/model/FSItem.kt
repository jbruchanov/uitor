package com.scurab.uitor.web.model

import com.scurab.uitor.common.util.ise
import kotlin.js.Json


class FSItem(val name: String, val size: String, val type: Int) {
    constructor(json: Json) : this(
        json["Name"] as? String ?: ise("Missing field 'Name'"),
        json["Size"] as? String ?: ise("Missing field 'Size'"),
        json["Type"] as? Int ?: ise("Missing field 'Type'")
    )
}