package com.scurab.uitor.web.inspector

import com.scurab.uitor.web.common.InspectorPage
import com.scurab.uitor.web.common.ViewPropertiesTableView
import com.scurab.uitor.web.model.PageViewModel
import com.scurab.uitor.web.ui.ColumnsLayout
import com.scurab.uitor.web.ui.IColumnsLayoutDelegate
import com.scurab.uitor.web.ui.ViewPropertiesTableViewComponents
import com.scurab.uitor.web.ui.table.TableData
import com.scurab.uitor.web.ui.table.TableViewDelegate
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.w3c.dom.Element
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLTableElement
import org.w3c.dom.get
import kotlin.browser.window
import kotlin.math.max
import kotlin.math.min

private const val ID_LEFT = "split-table-left"
private const val ID_MID = "split-table-mid"
private const val ID_RIGHT = "split-table-right"

class LayoutInspectorPage(
    pageViewModel: PageViewModel
) : InspectorPage(InspectorViewModel(pageViewModel)) {

    private lateinit var columnsLayout: ColumnsLayout
    private lateinit var canvasView: CanvasView
    private lateinit var treeView: TreeView
    private lateinit var propertiesView: ViewPropertiesTableView
    override var element: HTMLElement? = null; private set
    private val tableViewDelegate = TableViewDelegate(
        data = TableData.empty(),
        render = ViewPropertiesTableViewComponents.columnRenderer(pageViewModel.clientConfig)
    )

    override fun buildContent() {
        columnsLayout = ColumnsLayout(ColumnsLayoutDelegate(this))
        canvasView = CanvasView(viewModel)
        treeView = TreeView(viewModel)
        propertiesView = ViewPropertiesTableView(tableViewDelegate, viewModel.screenIndex)
    }

    override fun onAttachToRoot(rootElement: Element) {
        columnsLayout.attachTo(rootElement)
        canvasView.attachTo(columnsLayout.left)
        treeView.attachTo(columnsLayout.middle.first())
        propertiesView.attachTo(columnsLayout.right)
        element = columnsLayout.element
    }

    override fun onAttached() {
        super.onAttached()
        viewModel.apply {
            rootNode.observe {
                canvasView.renderMouseCross = true
                columnsLayout.initColumnSizes()
            }

            selectedNode.observe {
                propertiesView.viewNode = it
            }
        }
        GlobalScope.launch {
            canvasView.loadImage(viewModel.screenPreviewUrl)
        }
    }

    override fun onDetached() {
        canvasView.detach()
        treeView.detach()
        propertiesView.detach()
        super.onDetached()
    }

    class ColumnsLayoutDelegate(val page: LayoutInspectorPage) : IColumnsLayoutDelegate {
        override val innerContentWidthEstimator: (Int) -> Double = { column ->
            when(column) {
                2 -> {
                    ((page.treeView.element as? HTMLTableElement)?.rows?.get(0)?.getBoundingClientRect()?.width
                        ?: window.innerWidth / 4.0)
                }
                else -> max(500.0, min(window.innerWidth, window.screen.width) / 3.0)
            }
        }
    }
}