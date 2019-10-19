package com.scurab.uitor.web.inspector

import com.scurab.uitor.common.util.dlog
import com.scurab.uitor.web.model.ViewNode
import com.scurab.uitor.web.ui.HtmlView
import com.scurab.uitor.web.util.pickNodeForNotification
import com.scurab.uitor.web.util.scrollIntoViewArgs
import kotlinx.html.*
import kotlinx.html.dom.create
import kotlinx.html.js.onClickFunction
import kotlinx.html.js.onMouseOutFunction
import kotlinx.html.js.onMouseOverFunction
import kotlinx.html.js.table
import org.w3c.dom.Element
import org.w3c.dom.HTMLElement
import org.w3c.dom.events.Event
import kotlin.browser.document
import kotlin.dom.addClass
import kotlin.dom.clear
import kotlin.dom.removeClass

const val CSS_TREE = "tree"
const val CSS_ROW_EVEN = "tree-even"
const val CSS_ROW_ODD = "tree-odd"
const val CSS_TREE_CLASS_NAME = "tree-class_name"
const val CSS_TREE_SELECTED = "tree-selected"

class TreeView(
    private val rootElement: Element,
    private val inspectorViewModel: InspectorViewModel
) : HtmlView {
    private val TAG = "TreeView"
    private val tableRowToViewNodeMap = mutableListOf<ViewNode?>()
    private val styleTemplate: (Int) -> String = { "padding-left: calc(var( --tree-offset-left) * $it);" }
    private val clickCallback: (Event) -> Unit = {
        val vn = tableRowToViewNodeMap[it.viewNodeId]
        dlog(TAG) { "Clicked:${vn?.position}" }
        val node = pickNodeForNotification(inspectorViewModel.selectedNode.item, vn)
        inspectorViewModel.selectedNode.post(node)
    }
    private val hoverCallBack: (ViewNode?) -> Unit = {
        dlog(TAG) { "Hover:${it?.position}" }
        inspectorViewModel.hoveredNode.post(it)
    }
    private var lastHighlightedItem: ViewNode? = null
        set(value) {
            dlog(TAG) { "set lastHighlightedItem:${value?.position}" }
            field = value
        }
    private val mouseOverCallBack: (Event) -> Unit = {
        if (inspectorViewModel.selectedNode.item == null) {
            val vn = tableRowToViewNodeMap[it.viewNodeId]
            if (vn != lastHighlightedItem) {
                hoverCallBack(vn)
                lastHighlightedItem = vn
            }
        }
    }

    override lateinit var element: HTMLElement
        private set

    override fun attach(): TreeView {
        inspectorViewModel.rootNode.observe {
            rebuildHtml()
        }
        inspectorViewModel.selectedNode.observe { selectedNode ->
            dlog(TAG) { "inspectorViewModel.selectedNode:${selectedNode?.position}, ${lastHighlightedItem?.position}" }
            lastHighlightedItem?.element { vn ->
                removeClass(CSS_TREE_SELECTED)
                addClass(vn.position.evenOddStyle)
            }
            selectedNode?.element { vn ->
                removeClass(vn.position.evenOddStyle)
                addClass(CSS_TREE_SELECTED)
                scrollIntoView(scrollIntoViewArgs())
            }
            lastHighlightedItem = selectedNode
            dlog(TAG) { "New LastSelectedItem:${lastHighlightedItem?.position}" }
        }
        inspectorViewModel.hoveredNode.observe { hoveredNode ->
            if (inspectorViewModel.selectedNode.item == null) {
                lastHighlightedItem?.element { vn ->
                    removeClass(CSS_TREE_SELECTED)
                    addClass(vn.position.evenOddStyle)
                }
                lastHighlightedItem = hoveredNode?.element { vn ->
                    removeClass(vn.position.evenOddStyle)
                    addClass(CSS_TREE_SELECTED)
                }
            }
        }
        return this
    }

    private fun rebuildHtml() {
        tableRowToViewNodeMap.clear()
        rootElement.clear()
        val viewNode = inspectorViewModel.rootNode.item ?: return

        element = document.create.table {
            classes = setOf(CSS_TREE)
            viewNode.forEachIndexed { _, vn ->
                tr(classes = vn.position.evenOddStyle) {
                    id = vn.position.htmlViewNodeId
                    tableRowToViewNodeMap.add(vn)
                    onClickFunction = clickCallback
                    onMouseOverFunction = mouseOverCallBack
                    onMouseOutFunction = { hoverCallBack(null) }

                    td {
                        span { text("X") }
                    }
                    td {
                        span {
                            attributes["style"] = styleTemplate(vn.level)
                        }
                        span(classes = CSS_TREE_CLASS_NAME) { text(vn.typeSimple) }
                        span(classes = "tree-res-id") {
                            text(vn.ids?.takeIf { it != "undefined" }?.let { " [${it}]" } ?: "")
                        }
                    }

                }
            }
        }.apply {
            rootElement.append(this)
        }
    }

    private val Event.viewNodeId get() = (currentTarget as HTMLElement).id.substringAfter(":").toInt()
    private val Int.htmlViewNodeId get() = "ViewNode:$this"
    private val Int.evenOddStyle get() = if (this % 2 == 0) CSS_ROW_EVEN else CSS_ROW_ODD
    private inline fun ViewNode.element(block: HTMLElement.(ViewNode) -> Unit): ViewNode {
        val element = document.getElementById(position.htmlViewNodeId)
        (element as? HTMLElement)?.let { block(element, this) }
        return this
    }
}
