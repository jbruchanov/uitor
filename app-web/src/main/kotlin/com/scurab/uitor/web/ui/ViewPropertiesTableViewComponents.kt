package com.scurab.uitor.web.ui

import com.scurab.uitor.common.render.Color
import com.scurab.uitor.common.util.highlightAt
import com.scurab.uitor.common.util.ise
import com.scurab.uitor.common.util.matchingIndexes
import com.scurab.uitor.common.util.npe
import com.scurab.uitor.web.common.PropertiesViewRenderingContext
import com.scurab.uitor.web.model.ClientConfig
import com.scurab.uitor.web.ui.table.IRenderingContext
import com.scurab.uitor.web.ui.table.ITableViewRenderer
import kotlinx.html.TD
import kotlinx.html.TH
import kotlinx.html.a
import kotlinx.html.span
import kotlinx.html.unsafe

private const val INDEX_COLOR = 0
private const val INDEX_KEY = 1
private const val INDEX_VALUE = 2
private const val HTML_BOLD_START = "<b>"
private const val HTML_BOLD_END = "</b>"
private const val CSS_PROPERTIES_COLOR = "ui-table-view-properties-color"
private const val CSS_PROPERTIES_VALUE = "ui-table-view-properties-value"

object ViewPropertiesTableViewComponents {

    /**
     * Filter data based on smart property filter and property value contains
     */
    val filterAction: ((filter: String?, elements: List<Array<String>>) -> List<Array<String>>) =
        { filter, elements ->
            filter?.let {
                elements.filter { cols ->
                    val key = cols[INDEX_KEY].substringBefore(":")
                    val value = cols[INDEX_VALUE]

                    val matchingIndexes = key.matchingIndexes(filter)
                    val matchingFilter = (filter.isEmpty()
                            || matchingIndexes.isNotEmpty()) || value.contains(filter, true)

                    matchingFilter
                }
            } ?: elements
        }

    fun columnRenderer(
        clientConfig: ClientConfig
    ): ITableViewRenderer<String> {
        return ViewPropertiesTableViewRenderer(clientConfig)
    }
}

private class ViewPropertiesTableViewRenderer(
    clientConfig: ClientConfig
) : ITableViewRenderer<String> {
    private val propertyHighlights = clientConfig.propertyHighlights

    override val header: (TH.(IRenderingContext<String>, String?) -> Unit) = { _, value -> span { text(value ?: "") } }
    override val footer: (TH.(IRenderingContext<String>, String?) -> Unit)? = { _, value -> span { text(value ?: "") } }

    override val cell: TD.(IRenderingContext<String>, String) -> Unit = { context, value ->
        val (_, row, column) = context
        val context =
            context as? PropertiesViewRenderingContext ?: ise("rendering context is not PropertiesViewRenderingContext")
        val rowData = context.rowData ?: npe("RowData hasn't been set for Cell[$row, $column]")
        val filter = context.filter ?: ""
        val keyRaw = rowData[INDEX_KEY]
        val key = rowData[INDEX_KEY].substringBefore(":")
        when (column) {
            INDEX_COLOR -> key.toPropertyHighlightColor()
                ?.let {
                    span(classes = CSS_PROPERTIES_COLOR) {
                        attributes["style"] = "background-color:${it.htmlRGBA}"
                    }
                }
            INDEX_KEY -> {
                val link = keyRaw.endsWith(":")
                val matchingIndexes = key.matchingIndexes(filter)
                if (link) {
                    val position = context.viewNode?.position ?: npe("viewNode has to be set!")
                    val screenIndex = context.screenIndex
                    val url = "#ViewProperty?property=$key&screenIndex=${screenIndex}&position=${position}"
                    a(href = url, target = "_blank") {
                        span { unsafe { raw(key.highlightAt(matchingIndexes, HTML_BOLD_START, HTML_BOLD_END)) } }
                    }
                } else {
                    span { unsafe { raw(key.highlightAt(matchingIndexes, HTML_BOLD_START, HTML_BOLD_END)) } }
                }
            }
            INDEX_VALUE -> {
                span(classes = CSS_PROPERTIES_VALUE) {
                    unsafe { raw(value.highlightAt(filter, HTML_BOLD_START, HTML_BOLD_END)) }
                }
            }
        }
    }

    private fun String.toPropertyHighlightColor(): Color? {
        val v = this.toLowerCase()
        propertyHighlights.forEach { (r, c) ->
            if (r.matches(v)) {
                return c
            }
        }
        return null
    }
}