package com.scurab.uitor.web.ui

import com.scurab.uitor.common.util.highlightAt
import com.scurab.uitor.common.util.ise
import com.scurab.uitor.common.util.matchingIndexes
import com.scurab.uitor.common.util.npe
import com.scurab.uitor.web.common.PropertiesViewRenderingContext
import com.scurab.uitor.web.model.ClientConfig
import com.scurab.uitor.web.ui.ViewPropertiesTableViewComponents.INDEX_COLOR
import com.scurab.uitor.web.ui.ViewPropertiesTableViewComponents.INDEX_KEY
import com.scurab.uitor.web.ui.ViewPropertiesTableViewComponents.INDEX_VALUE
import com.scurab.uitor.web.ui.table.IRenderingContext
import com.scurab.uitor.web.ui.table.ITableViewRenderer
import com.scurab.uitor.web.util.styleAttributes
import com.scurab.uitor.web.util.styleBackgroundColor
import com.scurab.uitor.web.util.toPropertyHighlightColor
import kotlinx.html.TD
import kotlinx.html.TH
import kotlinx.html.a
import kotlinx.html.span
import kotlinx.html.unsafe

internal const val HTML_BOLD_START = "<b>"
internal const val HTML_BOLD_END = "</b>"
const val CSS_PROPERTIES_COLOR = "ui-table-view-properties-color"
private const val CSS_PROPERTIES_VALUE = "ui-table-view-properties-value"

object ViewPropertiesTableViewComponents {
    const val INDEX_COLOR = 0
    const val INDEX_KEY = 1
    const val INDEX_VALUE = 2

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

    val sortingMapper: (String) -> String = { it.toLowerCase() }

    fun columnRenderer(
        clientConfig: ClientConfig
    ): ITableViewRenderer<String> {
        return ViewPropertiesTableViewRenderer(clientConfig)
    }
}

private class ViewPropertiesTableViewRenderer(
    private val clientConfig: ClientConfig
) : ITableViewRenderer<String> {
    private val propertyHighlights = clientConfig.propertyHighlights
    private val valueRender = ViewPropertiesValueCellRenderer()
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
            INDEX_COLOR -> key.toPropertyHighlightColor(clientConfig.propertyHighlights)
                ?.let {
                    span(classes = CSS_PROPERTIES_COLOR) {
                        styleAttributes = it.styleBackgroundColor()
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
                    unsafe { +valueRender.renderValue(key, value, filter) }
                }
            }
        }
    }
}
