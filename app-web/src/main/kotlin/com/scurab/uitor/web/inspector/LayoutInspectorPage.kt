package com.scurab.uitor.web.inspector

import com.scurab.uitor.web.ServerApi
import com.scurab.uitor.web.model.ClientConfig
import com.scurab.uitor.web.ui.ColumnsLayout
import com.scurab.uitor.web.ui.HtmlView
import com.scurab.uitor.web.ui.IColumnsLayoutDelegate
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
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
    private val inspectorViewModel: InspectorViewModel
) : HtmlView() {

    private lateinit var columnsLayout: ColumnsLayout
    private lateinit var canvasView: CanvasView
    private lateinit var treeView: TreeView
    private lateinit var propertiesView: PropertiesView
    override lateinit var element: HTMLElement
    private val serverApi = ServerApi()

    override fun buildContent() {
        columnsLayout = ColumnsLayout(ColumnsLayoutDelegate(this))
        canvasView = CanvasView(inspectorViewModel)
        treeView = TreeView(inspectorViewModel)
        propertiesView = PropertiesView(inspectorViewModel)
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
        GlobalScope.launch {
            async {
                val item = serverApi.loadViewHierarchy(0)
                canvasView.renderMouseCross = true
                inspectorViewModel.rootNode.post(item)
                columnsLayout.initColumnSizes()
            }
            async {
                canvasView.loadImage("/screen.png")
            }
        }
    }

    class ColumnsLayoutDelegate(val page: LayoutInspectorPage) : IColumnsLayoutDelegate {
        override val innerContentWidthEstimator: (Int) -> Double = { column ->
            when(column) {
                2 -> {
                    7.0 + ((page.treeView.element as? HTMLTableElement)?.rows?.get(0)?.getBoundingClientRect()?.width
                        ?: window.innerWidth / 4.0)
                }
                else -> max(500.0, min(window.innerWidth, window.screen.width) / 3.0)
            }
        }
    }
}