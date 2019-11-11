package com.scurab.uitor.web.ui.viewproperties

import com.scurab.uitor.common.util.highlightAt
import com.scurab.uitor.common.util.iae
import com.scurab.uitor.common.util.ise
import com.scurab.uitor.common.util.matchingIndexes
import com.scurab.uitor.common.util.npe
import com.scurab.uitor.web.Navigation
import com.scurab.uitor.web.model.ClientConfig
import com.scurab.uitor.web.ui.viewproperties.ViewPropertiesTableViewComponents.GROOVY_NAME
import com.scurab.uitor.web.ui.viewproperties.ViewPropertiesTableViewComponents.INDEX_COLOR
import com.scurab.uitor.web.ui.viewproperties.ViewPropertiesTableViewComponents.INDEX_KEY
import com.scurab.uitor.web.ui.viewproperties.ViewPropertiesTableViewComponents.INDEX_VALUE
import com.scurab.uitor.web.ui.table.IRenderingContext
import com.scurab.uitor.web.ui.table.ITableDataItem
import com.scurab.uitor.web.ui.table.ITableViewRenderer
import com.scurab.uitor.web.ui.table.TableViewDelegate
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
    const val GROOVY_NAME = "Groovy Console:"

    /**
     * Filter data based on smart property filter and property value contains
     */
    val filterAction: ((filter: String?, elements: List<IViewPropertyTableItem>) -> List<IViewPropertyTableItem>) =
        { filter, elements ->
            filter?.let {
                elements.filter { item ->
                    val key = item.name.substringBefore(":")
                    val value = item.value

                    val matchingIndexes = key.matchingIndexes(filter)
                    val matchingFilter = (filter.isEmpty()
                            || matchingIndexes.isNotEmpty()) || value.contains(filter, true)

                    matchingFilter
                }
            } ?: elements
        }

    val sortingMapper: (column: Int, value: Any) -> String = { _, v -> v.toString().toLowerCase() }

    fun columnRenderer(clientConfig: ClientConfig, considerLinks: Boolean): ITableViewRenderer<IViewPropertyTableItem> {
        return ViewPropertiesTableViewRenderer(clientConfig, considerLinks)
    }

    fun defaultViewProperties(clientConfig: ClientConfig) = TableViewDelegate(
        columnRenderer(clientConfig, true)
    ).apply {
        sorting = true
        filtering = true
    }
}

interface IViewPropertyTableItem : ITableDataItem {
    val color: String
    val name: String
    val value: String
}

class ViewNodePropertyTableItem(
    override val color: String,
    override val name: String,
    override val value: String
) : IViewPropertyTableItem {
    override val tableColumns: Int = 3
    override fun get(column: Int): Any {
        return when (column) {
            0 -> color
            1 -> name
            2 -> value
            else -> iae("Invalid column:${column}, this object represents only $tableColumns")
        }
    }
}

private class ViewPropertiesTableViewRenderer(clientConfig: ClientConfig, private val considerLinks: Boolean) :
    ITableViewRenderer<IViewPropertyTableItem> {
    private val propertyHighlights = clientConfig.propertyHighlights
    private val valueRender = ViewPropertiesValueCellRenderer()
    override val header: (TH.(IRenderingContext<IViewPropertyTableItem>, String?) -> Unit) = { _, value -> span { text(value ?: "") } }
    override val footer: (TH.(IRenderingContext<IViewPropertyTableItem>, String?) -> Unit)? = null

    @Suppress("NAME_SHADOWING")
    override val cell: TD.(IRenderingContext<IViewPropertyTableItem>, Any) -> Unit = { context, value ->
        val (_, _, row, column) = context
        val item = context.item ?: npe("Item hasn't been set for Cell[$row, $column]")
        val filter = context.filter ?: ""
        val keyRaw = item.name
        val key = item.name.substringBefore(":")
        when (column) {
            INDEX_COLOR -> key.toPropertyHighlightColor(propertyHighlights)
                ?.let {
                    span(classes = CSS_PROPERTIES_COLOR) {
                        styleAttributes = it.styleBackgroundColor()
                    }
                }
            INDEX_KEY -> {
                val link = keyRaw.endsWith(":")
                val matchingIndexes = key.matchingIndexes(filter)
                if (considerLinks && link) {
                    val context =
                        context as? PropertiesViewRenderingContext ?: ise("rendering context is not PropertiesViewRenderingContext")
                    val position = context.viewNode?.position ?: npe("viewNode has to be set!")
                    val screenIndex = context.screenIndex
                    val url = if(GROOVY_NAME == keyRaw) {
                        Navigation.buildUrl("GroovyPage", "screenIndex" to screenIndex, "position" to position)
                    } else {
                        Navigation.buildUrl(
                            "ViewPropertyPage",
                            "property" to key,
                            "screenIndex" to screenIndex,
                            "position" to position
                        )
                    }
                    a(href = url, target = "_blank") {
                        span { unsafe { raw(key.highlightAt(matchingIndexes, HTML_BOLD_START, HTML_BOLD_END)) } }
                    }
                } else {
                    span { unsafe { raw(key.highlightAt(matchingIndexes, HTML_BOLD_START, HTML_BOLD_END)) } }
                }
            }
            INDEX_VALUE -> {
                span(classes = CSS_PROPERTIES_VALUE) {
                    unsafe { +valueRender.renderValue(key, value.toString(), filter) }
                }
            }
        }
    }
}
