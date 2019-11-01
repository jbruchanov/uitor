package com.scurab.uitor.web.common

import com.scurab.uitor.web.inspector.InspectorViewModel
import com.scurab.uitor.web.model.PageViewModel
import com.scurab.uitor.web.ui.ColumnsLayout
import com.scurab.uitor.web.ui.IColumnsLayoutDelegate
import com.scurab.uitor.web.ui.SEPARATOR_WIDTH
import com.scurab.uitor.web.ui.table.TableViewDelegate
import org.w3c.dom.Element
import org.w3c.dom.HTMLElement
import kotlin.browser.window
import kotlin.math.max

private const val COLUMN_COUNT = 2
private const val VIEW_PROPS_WIDTH = 600.0

abstract class BaseViewPropertiesPage(pageViewModel: PageViewModel) : InspectorPage(InspectorViewModel(pageViewModel)) {
    final override var element: HTMLElement? = null; private set

    abstract val contentElement: HTMLElement?

    private var viewPropertiesTableView = ViewPropertiesTableView(TableViewDelegate.default(viewModel.clientConfig), viewModel.screenIndex)
    private val columnsLayoutDelegate = object : IColumnsLayoutDelegate {
        override val innerContentWidthEstimator: (Int) -> Double = { column -> getColumnWidth(column) }
    }

    protected val columnsLayout = ColumnsLayout(columnsLayoutDelegate, COLUMN_COUNT)
    private var expandViewPropsColumn = true

    override fun onAttachToRoot(rootElement: Element) {
        super.onAttachToRoot(rootElement)
        columnsLayout.left.append(contentElement)
        viewPropertiesTableView.attachTo(columnsLayout.right)
    }

    override fun buildContent() {
        columnsLayout.buildContent()
        viewPropertiesTableView.buildContent()
        element = columnsLayout.element
    }

    override fun onAttached() {
        super.onAttached()
        columnsLayout.onAttached()
        viewModel.selectedNode.observe {
            viewPropertiesTableView.viewNode = it
            if (expandViewPropsColumn) {
                expandViewPropsColumn = false
                columnsLayout.setGridTemplateColumns("1fr 5px ${VIEW_PROPS_WIDTH}px")
                onColumnsResize(doubleArrayOf(getColumnWidth(0), SEPARATOR_WIDTH, getColumnWidth(1)))
            }
        }
        columnsLayout.onResize = this::onColumnsResize
        columnsLayout.initColumnSizes()
    }

    override fun onDetached() {
        columnsLayout.onResize = null
        super.onDetached()
    }

    protected fun getColumnWidth(column: Int): Double {
        val windowWidth = window.innerWidth
        val w = if (viewModel.selectedNode.item != null) VIEW_PROPS_WIDTH else 0.0
        val expWidth = if (column == 0) windowWidth - w else w
        val separatorWidths = ((COLUMN_COUNT - 1) * SEPARATOR_WIDTH)
        return max(0.0, expWidth - separatorWidths)
    }

    protected open fun onColumnsResize(sizes: DoubleArray) {
        //subclass
    }
}