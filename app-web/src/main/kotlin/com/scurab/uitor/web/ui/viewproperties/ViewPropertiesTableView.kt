package com.scurab.uitor.web.ui.viewproperties

import com.scurab.uitor.web.model.IClientConfig
import com.scurab.uitor.web.model.ViewNode
import com.scurab.uitor.web.ui.table.IRenderingContext
import com.scurab.uitor.web.ui.table.ITableViewDelegate
import com.scurab.uitor.web.ui.table.TableData
import com.scurab.uitor.web.ui.table.TableView
import com.scurab.uitor.web.util.toPropertyHighlightColor
import kotlinx.dom.clear

class PropertiesViewRenderingContext : IRenderingContext<IViewPropertyTableItem> {
    override var item: IViewPropertyTableItem? = null
    override var filter: String? = null
    override var row: Int = 0
    override var column: Int = 0
    var screenIndex: Int = 0
    var viewNode: ViewNode? = null
}

class ViewPropertiesTableView(
    private val clientConfig: IClientConfig,
    private val delegate: ITableViewDelegate<IViewPropertyTableItem>,
    private val screenIndex: Int
) : TableView<IViewPropertyTableItem>(TableData.empty(), delegate) {

    private val renderingContext = PropertiesViewRenderingContext()
    var viewNode: ViewNode? = null
        set(value) {
            field = value
            data = TableData(
                arrayOf("T", "Name", "Value"),
                value?.data
                    ?.addGroovyConsoleProperty(value.position)
                    ?.entries
                    ?.filter { it.value != null }
                    ?.sortedBy { ViewNode.orderKey(it.key) }
                    ?.map { entry ->
                        ViewNodePropertyTableItem(
                            entry.key.toPropertyHighlightColor(clientConfig.propertyHighlights)?.htmlRGB ?: "",
                            entry.key,
                            entry.value.toString()
                        ) as IViewPropertyTableItem
                    } ?: emptyList()
            ).apply {
                filterAction = ViewPropertiesTableViewComponents.filterAction
                sortingMapper = ViewPropertiesTableViewComponents.sortingMapper
                if (value != null) {
                    filter(filterValue)
                }
            }
        }

    override fun refreshContent() {
        if (viewNode != null) {
            super.refreshContent()
        } else {
            tableViewContainer.clear()
        }
    }

    override fun renderingContext(
        filter: String?,
        item: IViewPropertyTableItem?,
        row: Int,
        column: Int
    ): IRenderingContext<IViewPropertyTableItem> {
        return renderingContext.apply {
            this.filter = filter
            this.row = row
            this.column = column
            this.item = item
            this.viewNode = this@ViewPropertiesTableView.viewNode
            this.screenIndex = this@ViewPropertiesTableView.screenIndex
        }
    }

    private fun Map<String, Any?>.addGroovyConsoleProperty(position: Int): Map<String, Any?> {
        var result = this
        if (clientConfig.groovy) {
            result = toMutableMap().apply {
                this[ViewPropertiesTableViewComponents.GROOVY_NAME] = position
            }
        }
        return result
    }
}