package com.scurab.uitor.web.inspector

import com.scurab.uitor.common.render.Color
import com.scurab.uitor.common.util.dlog
import com.scurab.uitor.common.util.ref
import com.scurab.uitor.web.common.MOUSE_MIDDLE
import com.scurab.uitor.web.model.ViewNode
import com.scurab.uitor.web.model.isIgnored
import com.scurab.uitor.web.model.toggleIgnored
import com.scurab.uitor.web.ui.HtmlView
import com.scurab.uitor.web.util.pickNodeForNotification
import com.scurab.uitor.web.util.requireElementById
import com.scurab.uitor.web.util.scrollIntoViewArgs
import com.scurab.uitor.web.util.styleAttributes
import com.scurab.uitor.web.util.styleBackgroundColor
import kotlinx.html.classes
import kotlinx.html.div
import kotlinx.html.id
import kotlinx.html.js.onClickFunction
import kotlinx.html.js.onMouseDownFunction
import kotlinx.html.js.onMouseOutFunction
import kotlinx.html.js.onMouseOverFunction
import kotlinx.html.js.table
import kotlinx.html.span
import kotlinx.html.td
import kotlinx.html.tr
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLSpanElement
import org.w3c.dom.events.Event
import org.w3c.dom.events.MouseEvent
import kotlin.dom.addClass
import kotlin.dom.clear
import kotlin.dom.removeClass

const val CSS_TREE = "tree"
const val CSS_ROW_EVEN = "tree-even"
const val CSS_ROW_ODD = "tree-odd"
const val CSS_TREE_CLASS_NAME = "tree-class-name"
const val CSS_TREE_CLASS_NAME_IGNORED = "tree-class-name-ignored"
const val CSS_TREE_SELECTED = "tree-selected"
const val CSS_TREE_ID = "tree-res-id"
const val ID_TYPE_NAME = "tree-class-name"

class TreeView(
    private val inspectorViewModel: InspectorViewModel
) : HtmlView() {
    private val TAG = "TreeView"
    private val tableRowToViewNodeMap = mutableListOf<ViewNode?>()
    private val styleTemplate: (Int) -> String = { "padding-left: calc(var( --tree-offset-left) * $it);" }
    private var lastHighlightedItem: ViewNode? = null
        set(value) {
            dlog(TAG) { "set lastHighlightedItem:${value?.position}" }
            field = value
        }

    override var element: HTMLElement? = null; private set

    override fun buildContent() {
        //just empty stuff for now, will be removed later
        element = document.create.div { }
    }

    override fun onAttached() {
        super.onAttached()
        bind()
        rebuildHtml()
    }

    private fun bind() {
        inspectorViewModel.apply {
            rootNode.observe {
                rebuildHtml()
            }

            selectedNode.observe { selectedNode ->
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

            hoveredNode.observe { hoveredNode ->
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

            ignoredViewNodeChanged.observe {
                val (node, ignored) = it
                element.ref
                    .requireElementById<HTMLElement>(node.position.htmlViewNodeId)
                    .requireElementById<HTMLSpanElement>(ID_TYPE_NAME)
                    .className = node.typeNameClasses()
            }
        }
    }

    private fun rebuildHtml() {
        tableRowToViewNodeMap.clear()
        val viewNode = inspectorViewModel.rootNode.item ?: return
        val parentElement = parentElement ?: return
        parentElement.clear()

        element = document.create.table {
            classes = setOf(CSS_TREE)
            viewNode.forEachIndexed { _, vn ->
                tr(classes = vn.position.evenOddStyle) {
                    id = vn.position.htmlViewNodeId
                    tableRowToViewNodeMap.add(vn)
                    onClickFunction = clickAction
                    onMouseOverFunction = mouseOverAction
                    onMouseOutFunction = mouseOutAction
                    onMouseDownFunction = mouseDownAction
                    td {
                        span { text(" ") }
                    }
                    td {
                        span {
                            styleAttributes = styleTemplate(vn.level)
                        }
                        span(classes = vn.typeNameClasses()) {
                            id = ID_TYPE_NAME
                            vn.typeHighlightColor()?.let {
                                styleAttributes = it.styleBackgroundColor()
                            }
                            text(vn.typeSimple)
                        }
                        span(classes = CSS_TREE_ID) {
                            text(vn.ids
                                ?.takeIf { !(it == "undefined" || it == "View.NO_ID") }
                                ?.let { " [${it}]" } ?: "")
                        }
                    }
                }
            }
        }.apply {
            parentElement.append(this)
        }
    }

    //region event handlers
    private val clickAction: (Event) -> Unit = {
        val vn = tableRowToViewNodeMap[it.viewNodeId]
        dlog(TAG) { "Clicked:${vn?.position}" }
        val node = pickNodeForNotification(inspectorViewModel.selectedNode.item, vn)
        inspectorViewModel.selectedNode.post(node)
    }

    private val hoverAction: (ViewNode?) -> Unit = {
        dlog(TAG) { "Hover:${it?.position}" }
        inspectorViewModel.hoveredNode.post(it)
    }

    private val mouseOverAction: (Event) -> Unit = {
        if (inspectorViewModel.selectedNode.item == null) {
            val vn = tableRowToViewNodeMap[it.viewNodeId]
            if (vn != lastHighlightedItem) {
                hoverAction(vn)
                lastHighlightedItem = vn
            }
        }
    }

    private val mouseOutAction = { ev: Event -> hoverAction(null) }

    private val mouseDownAction = { ev: Event ->
        val ev = ev as MouseEvent
        if (ev.button == MOUSE_MIDDLE) {
            ev.preventDefault()
            tableRowToViewNodeMap[ev.viewNodeId]?.let { vn ->
                val ignored = vn.toggleIgnored(inspectorViewModel.ignoringViewNodeIdsOrPositions)
                inspectorViewModel.ignoredViewNodeChanged.post(Pair(vn, ignored))
            }
        }
    }
    //endregion

    private fun ViewNode.typeNameClasses(): String {
        var classes = CSS_TREE_CLASS_NAME
        if (isIgnored(inspectorViewModel.ignoringViewNodeIdsOrPositions)) {
            classes += " $CSS_TREE_CLASS_NAME_IGNORED"
        }
        return classes
    }

    private val Event.viewNodeId get() = (currentTarget as HTMLElement).id.substringAfter(":").toInt()
    private val Int.htmlViewNodeId get() = "ViewNode:$this"
    private val Int.evenOddStyle get() = if (this % 2 == 0) CSS_ROW_EVEN else CSS_ROW_ODD
    private inline fun ViewNode.element(block: HTMLElement.(ViewNode) -> Unit): ViewNode {
        val element = document.getElementById(position.htmlViewNodeId)
        (element as? HTMLElement)?.let { block(element, this) }
        return this
    }

    private fun ViewNode.typeHighlightColor(): Color? {
        return inspectorViewModel.clientConfig.typeHighlights[type]
    }
}
