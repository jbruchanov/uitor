package com.scurab.uitor.web.inspector

import com.scurab.uitor.common.util.dlog
import com.scurab.uitor.web.model.ViewNode
import kotlinx.html.classes
import kotlinx.html.dom.create
import kotlinx.html.js.onClickFunction
import kotlinx.html.js.table
import kotlinx.html.span
import kotlinx.html.td
import kotlinx.html.tr
import org.w3c.dom.Element
import org.w3c.dom.HTMLTableElement
import kotlin.browser.document
import kotlin.dom.clear

const val CSS_TREE = "tree"
const val CSS_ROW_EVEN = "tree-even"
const val CSS_ROW_ODD = "tree-odd"
const val CSS_TREE_CLASS_NAME = "tree-class_name"

class TreeView(private val rootElement: Element) {

    private val TAG = "TreeView"
    private var table: HTMLTableElement? = null

    var root: ViewNode? = null
        set(value) {
            field = value
            rebuildHtml()
        }

    private fun rebuildHtml() {
        val styleTemplate: (Int) -> String = { "padding-left: calc(var( --tree-offset-left) * $it);" }
        rootElement.clear()
        val viewNode = root ?: return

        document.create.table {
            classes = setOf(CSS_TREE)
            viewNode.forEachIndexed { i, vn ->
                tr(classes = if (i % 2 == 0) CSS_ROW_EVEN else CSS_ROW_ODD) {
                    onClickFunction = {
                        dlog(TAG) { "Clicked:${vn.position}" }
                    }
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
