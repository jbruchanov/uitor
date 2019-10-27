package com.scurab.uitor.web.util

import com.scurab.uitor.common.util.usingKey

private const val HASH = "#"
private const val DELIMITER = ":"
private const val KEYVALUE_DELIMITER = "="

class HashToken(private val token: String) {
    val pageId: String
    private val args = mutableMapOf<String, String>()
    val type: String? by args.usingKey("type") { null }
    val screenIndex: String? by args.usingKey("screenIndex") { null }

    init {
        val value = token.substringAfter(HASH, "")
        val items = value.split(DELIMITER)
        pageId = items.firstOrNull()?.substringBefore(DELIMITER) ?: "MainPage"
        for (i in (1 until items.size)) {
            val arg = items[i].split(KEYVALUE_DELIMITER)
            if (arg.size == 2) {
                args[arg[0]] = arg[1]
            }
        }
    }

    fun append(key: String, value: String): HashToken {
        args[key] = value
        return this
    }

    override fun toString(): String {
        val args = args.entries.joinToString(separator = DELIMITER) { (k, v) -> "$k$KEYVALUE_DELIMITER${v}" }
        return "$HASH${pageId}" + args.takeIf { it.isNotEmpty() }?.let { DELIMITER + it } ?: ""
    }
}