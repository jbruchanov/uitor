package com.scurab.uitor.web.ui.viewproperties

import com.scurab.uitor.common.util.iae
import com.scurab.uitor.common.util.ise
import com.scurab.uitor.common.util.prefixToLen
import com.scurab.uitor.common.util.ref
import com.scurab.uitor.web.common.Page
import com.scurab.uitor.web.inspector.InspectorViewModel
import com.scurab.uitor.web.ui.TabDataProvider
import com.scurab.uitor.web.ui.TabHtmlView
import com.scurab.uitor.web.ui.table.ITableDataItem
import com.scurab.uitor.web.ui.table.TableData
import com.scurab.uitor.web.ui.table.TableView
import com.scurab.uitor.web.ui.table.TableViewDelegate
import com.scurab.uitor.web.ui.viewproperties.ViewPropertiesTableViewComponents.defaultViewProperties
import kotlinx.html.js.div
import org.w3c.dom.Element
import org.w3c.dom.HTMLElement

/**
 * A view representing Tabs for view properties and whole screen view stats
 */
class ViewPropsStatsView(private val viewModel: InspectorViewModel) : Page() {
    override fun stateDescription(): String? = null

    override var element: HTMLElement? = null; private set
    private val viewPropertiesTableView = ViewPropertiesTableView(
        viewModel.clientConfig,
        defaultViewProperties(viewModel.clientConfig, viewModel.serverApi.supportsViewPropertyDetails),
        viewModel.screenIndex
    )
    private val viewStatsTableView = TableView(delegate = TableViewDelegate<PropertyTableItem>().apply {
        elementId = "view-properties-stats"
        sorting = true
    })
    private val tabHtmlView = TabHtmlView(TabDataProvider(
        2,
        name = {
            when (it) {
                0 -> "Properties"
                1 -> "Stats"
                else -> ise("Invalid column:$it")
            }
        },
        creator = {
            when (it) {
                0 -> viewPropertiesTableView
                1 -> viewStatsTableView
                else -> ise("Invalid column:$it")
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
        viewPropertiesTableView.onAttached()
        viewModel.selectedNode.observe { node ->
            viewPropertiesTableView.viewNode = node
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
                ).apply {
                    sortingMapper = { column, value ->
                        when (column) {
                            0 -> value.toString()
                            1 -> value.toString().prefixToLen('0', 3)
                            else -> iae("Invalid column:${column}")
                        }
                    }
                }
                viewStatsTableView.data = data
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