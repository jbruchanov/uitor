package com.scurab.uitor.web.ui

import com.scurab.uitor.common.util.dlog
import com.scurab.uitor.web.addMouseDownListener
import com.scurab.uitor.web.addMouseLeaveListener
import com.scurab.uitor.web.addMouseMoveListener
import com.scurab.uitor.web.addMouseUpListener
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

class ColumnsLayout(private val root: Element) : HtmlView {

    val columns: Int = 3//not fully ready for != 3
    val left: Element by lazy { document.requireElementById(ID_LEFT) }
    val middle: Array<Element> by lazy { document.requireElementsByClass(CLASS_MIDDLE) }
    val right: Element by lazy { document.requireElementById(ID_RIGHT) }

    init {
        check(columns >= 2) { "Min amount of columns is 2, not $columns" }
    }

    override lateinit var element: HTMLElement
        private set

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
            ResizableColumnsFeature(this@ColumnsLayout).attach()
        }
        return this
        //TODO inject styling
    }
}

/**
 * Simple and naive column drag resizing
 */
private class ResizableColumnsFeature(private val splitTableView: ColumnsLayout) {
    private val TAG = "ResizableColumnsFeature"
    private val separators = document.requireElementsByClass(CLASS_SEPARATOR)
    private var draggingElement: Element? = null
    private var draggingIndex = -1
    private var downX = -1

    fun attach() {
        separators.forEach { sep ->
            sep.addMouseDownListener { de ->
                downX = de.clientX
                draggingElement = de.target as? Element
                draggingIndex = draggingElement?.let { it.parentElement?.children?.indexOf(it) } ?: -1
                dlog(TAG) { "MouseDown x:$downX draggingIndex:$draggingIndex elem:$draggingElement" }
            }

            document.addMouseLeaveListener {
                dlog(TAG) { "MouseLeave [${it.clientX},${it.clientY}]" }
                stopDragging()
            }

            document.addMouseUpListener {
                dlog(TAG) { "MouseUp [${it.clientX},${it.clientY}]" }
                stopDragging()
            }

            val sizes = Array(splitTableView.columns) { 0.0 }
            document.addMouseMoveListener { me ->
                val parent = draggingElement?.parentElement as? HTMLElement ?: return@addMouseMoveListener
                parent.children.let {
                    it.forEachIndexed { el, i ->
                        sizes[i] = el.getBoundingClientRect().width
                    }
                }
                val diff = me.clientX - downX
                if (diff != 0) {
                    sizes[draggingIndex - 1] = max(0.0, sizes[draggingIndex - 1] + diff)
                    sizes[draggingIndex + 1] = max(0.0, sizes[draggingIndex + 1] - diff)
                    if (sizes[draggingIndex - 1] <= 0.0 || sizes[draggingIndex + 1] <= 0.0) {
                        stopDragging()
                    }
                    downX = me.clientX
                    val result = sizes.joinToString("px ", postfix = "px")
                    dlog(TAG) { "MouseMove [${me.clientX},${me.clientY}] diff:$diff result:$result" }
                    splitTableView.element.style.setProperty("grid-template-columns", result)
                }
            }
        }
    }

    private fun stopDragging() {
        dlog(TAG) { "stopDragging" }
        draggingIndex = -1
        draggingElement = null
    }
}