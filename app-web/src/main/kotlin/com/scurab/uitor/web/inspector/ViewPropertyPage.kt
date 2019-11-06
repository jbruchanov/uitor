package com.scurab.uitor.web.inspector

import com.scurab.uitor.common.util.elog
import com.scurab.uitor.common.util.iae
import com.scurab.uitor.common.util.messageSafe
import com.scurab.uitor.web.common.Page
import com.scurab.uitor.web.resources.ResourcesContentGenerator
import com.scurab.uitor.web.ui.IViewPropertyTableItem
import com.scurab.uitor.web.ui.ViewPropertiesTableViewComponents
import com.scurab.uitor.web.ui.launchWithProgressBar
import com.scurab.uitor.web.ui.table.TableData
import com.scurab.uitor.web.ui.table.TableView
import com.scurab.uitor.web.ui.table.TableViewDelegate
import com.scurab.uitor.web.util.HashToken
import com.scurab.uitor.web.util.lazyLifecycled
import com.scurab.uitor.web.util.toPropertyHighlightColor
import kotlinx.html.button
import kotlinx.html.div
import kotlinx.html.id
import kotlinx.html.js.div
import kotlinx.html.js.onClickFunction
import org.w3c.dom.Element
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement
import kotlin.browser.window

private const val ID_PROPERTIES_CONTAINER = "view-property-container"
private const val ID_PROPERTIES_TABLE_CONTAINER = "view-property-properties-container"
private const val ID_DATA_PREVIEW_CONTAINER = "view-property-preview-container"

class ViewPropertyPage(
    private val position: Int,
    private val property: String,
    private val viewModel: InspectorViewModel
) : Page() {

    private val TAG = "ViewPropertyPage"
    override fun stateDescription(): String? = "screenIndex=${viewModel.screenIndex}${HashToken.DELIMITER}" +
            "position=${position}${HashToken.DELIMITER}" +
            "property=${property}"

    override var element: HTMLElement? = null; private set

    private val resourcesContentGenerator = ResourcesContentGenerator()
    private val tableViewContainer by lazyLifecycled { requireElementById<HTMLDivElement>(ID_PROPERTIES_TABLE_CONTAINER) }
    private val contentContainer by lazyLifecycled { requireElementById<HTMLDivElement>(ID_DATA_PREVIEW_CONTAINER) }
    private val tableViewDelegate = TableViewDelegate(
        render = ViewPropertiesTableViewComponents.columnRenderer(viewModel.clientConfig, false)
    ).apply {
        sorting = true
        filtering = true
    }
    private var viewPropertiesTableView = TableView(
        TableData.empty(),
        delegate = tableViewDelegate
    )

    override fun buildContent() {
        element = document.create.div {
            id = ID_PROPERTIES_CONTAINER
            div {
                button {
                    text("Raw JSON")
                    onClickFunction = { onOpenRawJson() }
                }
                div {
                    id = ID_PROPERTIES_TABLE_CONTAINER
                }
            }
            div {
                id = ID_DATA_PREVIEW_CONTAINER
            }
        }
    }

    override fun onAttachToRoot(rootElement: Element) {
        super.onAttachToRoot(rootElement)
        viewPropertiesTableView.attachTo(tableViewContainer)
        launchWithProgressBar {
            try {
                val item = viewModel.serverApi.loadViewProperty(viewModel.screenIndex, position, property)
                viewPropertiesTableView.data = TableData(
                    headers = arrayOf("T", "Name", "Value"),
                    initElements = item.properties.map {(name, value) ->
                        ViewPropertyTableItem(name, value.toString()) as IViewPropertyTableItem
                    }
                ).apply {
                    filterAction = ViewPropertiesTableViewComponents.filterAction
                }
                contentContainer.append(resourcesContentGenerator.buildContent(item))
            } catch (e: Throwable) {
                alert("Unable to load view property:${property}")
                elog(TAG) { e.messageSafe }
            }
        }
    }

    private fun onOpenRawJson() {
        val url = viewModel.serverApi.viewPropertyUrl(viewModel.screenIndex, position, property, 3)
        window.open(url, "_blank", "")
    }

    inner class ViewPropertyTableItem(
        override val name: String,
        override val value: String) : IViewPropertyTableItem {
        override val color = name.toPropertyHighlightColor(viewModel.clientConfig.propertyHighlights)?.htmlRGB ?: ""

        override fun get(column: Int): Any = when (column) {
            0 -> color
            1 -> name
            2 -> value
            else -> iae("Invalid column:${column}")
        }
        override val tableColumns: Int = 3
    }
}