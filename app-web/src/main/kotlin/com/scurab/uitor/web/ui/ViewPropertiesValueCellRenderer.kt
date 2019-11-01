package com.scurab.uitor.web.ui

import com.scurab.uitor.common.render.toColor
import com.scurab.uitor.common.util.highlightAt
import com.scurab.uitor.common.util.isUppercase

private typealias HtmlString = String
private typealias Renderer = (key: String, value: String, hightlighted: String) -> HtmlString

private const val CSS_KEYWORD = "view-property-code-keyword"
private const val CSS_STATIC_VALUE = "view-property-code-static-value"
private const val CSS_HIGHLIGHT = "view-property-code-highlight"
private const val CSS_NUMBER = "view-property-code-number"
private val codeValues = listOf("true", "false", "null").sorted()

class ViewPropertiesValueCellRenderer {
    fun renderValue(key: String, value: String, filter: String?): String {
        val renderer = renders[key] ?: defaultRender
        val hightlighted = filter?.takeIf { it.isNotEmpty() }
            ?.let { value.highlightAt(it, HTML_BOLD_START, HTML_BOLD_END) }
            ?: value
        val out = renderer(key, value, hightlighted)
        return out
    }

    companion object {
        val renders = mapOf<String, Renderer>(
            "Inheritance" to { k, v, h -> h.replace(" >", " &gt;<br/>") },
            "Context" to { k, v, h -> span(CSS_HIGHLIGHT, v, h) },
            "Owner" to { k, v, h -> span(CSS_HIGHLIGHT, v, h) },
            "IDs" to { k, v, h -> span(CSS_HIGHLIGHT, v, h) }
        )
        val defaultRender: Renderer = { k, v, h ->
            when {
                v.startsWith("com.android") || v.startsWith("android") -> googleLink(v, h)
                isKeyword(v) -> span(CSS_KEYWORD, v, h)
                isStaticValue(v) -> span(CSS_STATIC_VALUE, v, h)
                isNumber(v) -> span(CSS_NUMBER, v, h)
                isColor(v) -> color(v, h)
                else -> h
            }
        }

        private fun isKeyword(value: String): Boolean = codeValues.binarySearch(value) >= 0
        private fun isStaticValue(value: String): Boolean = value.first().isUppercase() && value.last().isUppercase()
        private fun isNumber(value: String): Boolean = value.toDoubleOrNull()?.isFinite() == true
        private fun isColor(value: String): Boolean = value.startsWith("#") && value.length in (4..9)
        private fun htmlLink(value: String, url: String): String = """<a href="$url" target="_blank">$value</a>"""
        private fun googleLink(value: String, highlighted: String) =
            htmlLink(
                highlighted,
                "https://developer.android.com/s/results/?q=${value.substringBefore('@').substringBefore('{')}"
            )

        private fun span(css: String, value: String, highlighted: String): String {
            return """<span class="$css">$highlighted</span>"""
        }

        private fun color(value: String, highlighted: String): String {
            val color = value.toColor()
            return """<div>${highlighted}<span class="view-property-color-preview-parent">
                <span class="view-property-color-preview" style="background: ${color.htmlRGBA};">&nbsp;</span>
                </span></div>""".trimIndent()
        }
    }
}