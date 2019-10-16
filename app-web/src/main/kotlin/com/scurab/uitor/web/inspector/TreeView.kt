package com.scurab.uitor.web.inspector

import com.scurab.uitor.common.util.dlog
import com.scurab.uitor.web.model.ViewNode
import kotlinx.html.*
import kotlinx.html.dom.create
import kotlinx.html.js.onClickFunction
import kotlinx.html.js.onMouseOutFunction
import kotlinx.html.js.onMouseOverFunction
import kotlinx.html.js.table
import org.w3c.dom.Element
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLTableElement
import org.w3c.dom.events.Event
import org.w3c.dom.events.MouseEvent
import kotlin.browser.document
import kotlin.dom.clear

const val CSS_TREE = "tree"
const val CSS_ROW_EVEN = "tree-even"
const val CSS_ROW_ODD = "tree-odd"
const val CSS_TREE_CLASS_NAME = "tree-class_name"

class TreeView(
    private val rootElement: Element,
    private val inspectorViewModel: InspectorViewModel
) {

    private val TAG = "TreeView"
    private var table: HTMLTableElement? = null

    init {
        inspectorViewModel.rootNode.observe {
            rebuildHtml()
        }
    }

    private fun rebuildHtml() {
        val styleTemplate: (Int) -> String = { "padding-left: calc(var( --tree-offset-left) * $it);" }
        rootElement.clear()
        val viewNode = inspectorViewModel.rootNode.item ?: return

        val tableRowToViewNodeMap = mutableMapOf<String, ViewNode>()
        val clickCallback: (Event) -> Unit = {
            val vn = tableRowToViewNodeMap[(it.currentTarget as HTMLElement).id]
            dlog(TAG) { "Clicked:${vn?.position}" }
            inspectorViewModel.selectedNode.post(vn)
        }

        val hoverCallBack: (ViewNode?) -> Unit = {
            dlog(TAG) { "Hover:${it?.position}" }
            inspectorViewModel.hoveredNode.post(it)
        }

        val mouseCallBack: (Event) -> Unit = {
            val vn = tableRowToViewNodeMap[(it.currentTarget as HTMLElement).id]
            hoverCallBack(vn)
        }

        document.create.table {
            classes = setOf(CSS_TREE)
            viewNode.forEachIndexed { i, vn ->
                tr(classes = if (i % 2 == 0) CSS_ROW_EVEN else CSS_ROW_ODD) {
                    id = "ViewNode:$i"
                    tableRowToViewNodeMap[id] = vn
                    onClickFunction = clickCallback
                    onMouseOverFunction = mouseCallBack
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
}
