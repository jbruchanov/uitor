package com.scurab.uitor.web.ui

import com.scurab.uitor.common.util.dlog
import com.scurab.uitor.web.*
import com.scurab.uitor.web.util.forEachIndexed
import com.scurab.uitor.web.util.indexOf
import com.scurab.uitor.web.util.requireElementById
import com.scurab.uitor.web.util.requireElementsByClass
import kotlinx.html.div
import kotlinx.html.dom.create
import kotlinx.html.id
import kotlinx.html.js.div
import org.w3c.dom.Element
import org.w3c.dom.HTMLElement
import kotlin.browser.document
import kotlin.math.max

private const val ID_LEFT = "split-table-left"
private const val ID_MID = "split-table-mid"
private const val ID_RIGHT = "split-table-right"
private const val CLASS_MIDDLE = "middle"
private const val CLASS_SEPARATOR = "split-table-separator"
private const val CLASS_COLUMN = "split-table-column"
private const val GRID_TEMPLATE_COLUMNS = "grid-template-columns"

class ColumnsLayout(private val root: Element,
                    private val delegate: IColumnsLayoutDelegate
) : HtmlView {

    val columns: Int = 3//not fully ready for != 3
    val left: Element by lazy { document.requireElementById(ID_LEFT) }
    val middle: Array<Element> by lazy { document.requireElementsByClass(CLASS_MIDDLE) }
    val right: Element by lazy { document.requireElementById(ID_RIGHT) }

    init {
        check(columns >= 2) { "Min amount of columns is 2, not $columns" }
    }

    override lateinit var element: HTMLElement
        private set
    private val resizableColumnsFeature = ResizableColumnsFeature(this, delegate.innerContentWidthEstimator)

    override fun attach(): ColumnsLayout {
        element = document.create.div("split-table") {
            div("$CLASS_COLUMN left") { id = ID_LEFT }
            div(CLASS_SEPARATOR)
            for (n in columns - 2 downTo 1) {
                div("$CLASS_COLUMN $CLASS_MIDDLE") {
                    if (columns == 3) {
                        id = ID_MID
                    }
                }
                div(CLASS_SEPARATOR)
            }
            div("$CLASS_COLUMN right") { id = ID_RIGHT }
        }.apply {
            root.append(this)
            resizableColumnsFeature.attach()
        }
        return this
        //TODO inject styling
    }

    fun initColumnSizes() {
        resizableColumnsFeature.initColumnSizes()
    }
}

interface IColumnsLayoutDelegate {
    val innerContentWidthEstimator: (Int) -> Double
}

/**
 * Simple and naive column drag resizing
 */
private class ResizableColumnsFeature(private val splitTableView: ColumnsLayout,
                                      private val widthEstimator: (Int) -> Double
) {
    private val TAG = "ResizableColumnsFeature"
    private var draggingElement: Element? = null
    private var draggingIndex = -1
    private var downX = -1
    private lateinit var separators: Array<Element>
    //columns + separators in between them
    private val sizes = DoubleArray((2 * splitTableView.columns) - 1)

    fun attach() : ResizableColumnsFeature {
        separators = document.requireElementsByClass(CLASS_SEPARATOR)
        separators.forEach { sep ->
            sep.addMouseDownListener { de ->
                downX = de.clientX
                draggingElement = de.target as? Element
                draggingIndex = draggingElement?.let { it.parentElement?.children?.indexOf(it) } ?: -1
                dlog(TAG) { "MouseDown x:$downX draggingIndex:$draggingIndex elem:$draggingElement" }
            }
            sep.addMouseDoubleClickListener { de ->
                val draggingElement = de.target as? Element
                val draggingIndex = draggingElement?.let { it.parentElement?.children?.indexOf(it) } ?: -1
                refreshColumnsSizes(draggingElement)
                resizeColumnsDoubleClick(draggingIndex, widthEstimator(draggingIndex + 1))
                dlog(TAG) {
                    "MouseDoubleClick [${de.clientX},${de.clientY}] " +
                            "result:${splitTableView.element.style.getPropertyValue(GRID_TEMPLATE_COLUMNS)}"
                }
            }

            document.addMouseLeaveListener {
                dlog(TAG) { "MouseLeave [${it.clientX},${it.clientY}]" }
                stopDragging()
            }

            document.addMouseUpListener {
                dlog(TAG) { "MouseUp [${it.clientX},${it.clientY}]" }
                stopDragging()
            }

            document.addMouseMoveListener { me ->
                val diff = me.clientX - downX
                if (draggingElement != null) {
                    resizeColumnsDragging(draggingIndex, diff)
                    downX = me.clientX
                    dlog(TAG) {
                        "MouseMove [${me.clientX},${me.clientY}] diff:$diff " +
                                "result:${splitTableView.element.style.getPropertyValue(GRID_TEMPLATE_COLUMNS)}"
                    }
                }
            }
        }
        return this
    }

    fun initColumnSizes() {
        //take the table itself not the root
        refreshColumnsSizes(splitTableView.element.firstElementChild)
        //TODO something better
        val sum = sizes.asList().subList(1, sizes.size - 1).sum()
        sizes[sizes.size - 1] = widthEstimator.invoke(sizes.size - 1)
        sizes[sizes.size - 3] = widthEstimator.invoke(sizes.size - 3)
        sizes[0] += (sum - sizes.asList().subList(1, sizes.size - 1).sum())
        val result = sizes.joinToString("px ", postfix = "px")
        splitTableView.element.style.setProperty(GRID_TEMPLATE_COLUMNS, result)
    }

    private fun refreshColumnsSizes(draggingElement: Element?) {
        val parent = draggingElement?.parentElement as? HTMLElement ?: return
        parent.children.let {
            it.forEachIndexed { el, i ->
                sizes[i] = el.getBoundingClientRect().width
            }
        }
    }

    private fun resizeColumnsDoubleClick(draggingIndex: Int, desiredWidth: Double, sizes: DoubleArray = this.sizes) {
        sizes.apply {
            val diff = sizes[draggingIndex + 1] - desiredWidth
            sizes[draggingIndex - 1] += diff
            sizes[draggingIndex + 1] -= diff
        }
        val result = sizes.joinToString("px ", postfix = "px")
        splitTableView.element.style.setProperty(GRID_TEMPLATE_COLUMNS, result)
    }

    private fun resizeColumnsDragging(draggingIndex: Int, diff: Int, sizes: DoubleArray = this.sizes) {
        if (diff != 0) {
            sizes[draggingIndex - 1] = max(0.0, sizes[draggingIndex - 1] + diff)
            sizes[draggingIndex + 1] = max(0.0, sizes[draggingIndex + 1] - diff)
            if (sizes[draggingIndex - 1] <= 0.0 || sizes[draggingIndex + 1] <= 0.0) {
                stopDragging()
            }
            val result = sizes.joinToString("px ", postfix = "px")
            splitTableView.element.style.setProperty(GRID_TEMPLATE_COLUMNS, result)
        }
    }

    private fun stopDragging() {
        dlog(TAG) { "stopDragging" }
        draggingIndex = -1
        draggingElement = null
    }
}