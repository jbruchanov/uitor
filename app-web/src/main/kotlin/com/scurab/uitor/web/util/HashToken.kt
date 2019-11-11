package com.scurab.uitor.web.util

import com.scurab.uitor.common.util.usingKey
import kotlin.browser.window

class HashToken(private val token: String = window.location.hash) {
    val pageId: String
    val arguments = mutableMapOf<String, String>()
    val type: String? by arguments.usingKey(TYPE) { null }
    val screenIndex: String? by arguments.usingKey(SCREEN_INDEX) { null }

    init {
        val value = token.substringAfter(HASH, "")
        val items = value.split(DELIMITER)
        pageId = items.firstOrNull()?.substringBefore(DELIMITER) ?: "MainPage"
        for (i in (1 until items.size)) {
            val arg = items[i].split(KEY_VALUE_DELIMITER)
            if (arg.size == 2) {
                arguments[arg[0]] = arg[1]
            }
        }
    }

    fun append(key: String, value: String): HashToken {
        arguments[key] = value
        return this
    }

    override fun toString(): String {
        val args = arguments.entries.joinToString(separator = DELIMITER) { (k, v) -> "$k$KEY_VALUE_DELIMITER${v}" }
        return "$HASH${pageId}" + (args.takeIf { it.isNotEmpty() }?.let { DELIMITER + it } ?: "")
    }

    companion object {
        const val DELIMITER = ":"
        const val HASH = "#"
        const val SCREEN_INDEX = "screenIndex"
        const val TYPE = "type"
        private const val KEY_VALUE_DELIMITER = "="
        fun state(vararg pairs: Pair<String, Any>): String {
            return pairs.joinToString(separator = DELIMITER) { (k, v) -> "$k$KEY_VALUE_DELIMITER$v" }
        }
    }
}