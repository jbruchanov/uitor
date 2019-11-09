package com.scurab.uitor.web.common

import com.scurab.uitor.common.util.ref
import com.scurab.uitor.web.inspector.InspectorViewModel
import com.scurab.uitor.web.ui.TabDataProvider
import com.scurab.uitor.web.ui.TabHtmlView
import com.scurab.uitor.web.ui.table.ITableDataItem
import com.scurab.uitor.web.ui.table.TableData
import com.scurab.uitor.web.ui.table.TableView
import com.scurab.uitor.web.ui.table.TableViewDelegate
import kotlinx.html.js.div
import org.w3c.dom.Element
import org.w3c.dom.HTMLElement

class ViewPropsStatsView(private val viewModel: InspectorViewModel) : Page() {
    override fun stateDescription(): String? = null

    override var element: HTMLElement? = null; private set
    private val tableView = ViewPropertiesTableView(
        viewModel.clientConfig,
        BaseViewPropertiesPage.defaultViewProperties(viewModel.clientConfig),
        viewModel.screenIndex
    )
    private val tableView2 = TableView(delegate = TableViewDelegate<PropertyTableItem>().apply {
        sorting = true
    })
    private val tabHtmlView = TabHtmlView(TabDataProvider(
        2,
        {
            when (it) {
                0 -> "Properties"
                1 -> "Stats"
                else -> ""
            }
        }, {
            when (it) {
                0 -> tableView
                1 -> tableView2
                else -> TODO()
            }
        }
    ))

    override fun buildContent() {
        element = document.create.div {}
        tabHtmlView.buildContent()
    }

    override fun onAttachToRoot(rootElement: Element) {
        super.onAttachToRoot(rootElement)
        tabHtmlView.attachTo(element.ref)
    }

    override fun onAttached() {
        super.onAttached()
        tableView.onAttached()
        viewModel.selectedNode.observe { node ->
            tableView.viewNode = node
        }
        viewModel.rootNode.observe { node ->
            node?.let { vn ->
                val all = vn.all()
                val items = all
                    .groupBy { it.typeSimple }
                    .map { PropertyTableItem(it.key, it.value.size) }
                    .sortedBy { it.name }

                val data = TableData(
                    arrayOf("Name", "Count"),
                    initElements = items,
                    footers = arrayOf("Sum", all.size.toString())
                )
                tableView2.data = data
            }
        }
        //no need to load, already done by parent
    }
}

private class PropertyTableItem(val name: String, val size: Int) : ITableDataItem {
    override fun get(column: Int): Any {
        return when (column) {
            0 -> name
            1 -> size.toString()
            else -> ""
        }
    }

    override val tableColumns: Int get() = 2
}