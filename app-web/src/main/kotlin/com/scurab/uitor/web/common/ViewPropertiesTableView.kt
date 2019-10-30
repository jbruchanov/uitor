package com.scurab.uitor.web.common

import com.scurab.uitor.web.model.ViewNode
import com.scurab.uitor.web.ui.ViewPropertiesTableViewComponents
import com.scurab.uitor.web.ui.table.IRenderingContext
import com.scurab.uitor.web.ui.table.ITableViewDelegate
import com.scurab.uitor.web.ui.table.Row
import com.scurab.uitor.web.ui.table.TableData
import com.scurab.uitor.web.ui.table.TableView
import kotlin.dom.clear

class PropertiesViewRenderingContext : IRenderingContext<String> {
    override var filter: String? = null
    override var row: Int = 0
    override var column: Int = 0
    var screenIndex: Int = 0
    var rowData: Row<String>? = null
    var viewNode: ViewNode? = null
}

class ViewPropertiesTableView(
    private val delegate: ITableViewDelegate<String>,
    private val screenIndex: Int
) : TableView<String>(delegate) {

    private val renderingContext = PropertiesViewRenderingContext()
    var viewNode: ViewNode? = null
        set(value) {
            field = value
            delegate.data = TableData(
                arrayOf("", "Name", "Value"),
                value?.data
                    ?.entries
                    ?.filter { it.value != null }
                    ?.sortedBy { ViewNode.orderKey(it.key) }
                    ?.map { entry -> arrayOf("", entry.key, entry.value.toString()) } ?: emptyList()
            ).apply {
                filterAction = ViewPropertiesTableViewComponents.filterAction
                if (value != null) {
                    filter(filterValue)
                }
            }
            refreshContent()
        }

    override fun refreshContent() {
        if (viewNode != null) {
            super.refreshContent()
        } else {
            tableViewContainer.clear()
        }
    }

    override fun renderingContext(filter: String?, row: Int, column: Int): IRenderingContext<String> {
        return renderingContext.apply {
            this.filter = filter
            this.row = row
            this.column = column
            this.rowData = row.takeIf { row in (0 until delegate.data.rows) }?.let { delegate.data.row(it) }
            this.viewNode = this@ViewPropertiesTableView.viewNode
            this.screenIndex = this@ViewPropertiesTableView.screenIndex
        }
    }
}