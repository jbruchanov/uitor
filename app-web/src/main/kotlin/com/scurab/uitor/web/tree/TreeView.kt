package com.scurab.uitor.web.tree

import com.scurab.uitor.web.inspector.InspectorViewModel
import com.scurab.uitor.web.model.ViewNode
import com.scurab.uitor.web.ui.HtmlView
import com.scurab.uitor.web.util.requireElementById
import kotlinx.html.button
import kotlinx.html.div
import kotlinx.html.dom.create
import kotlinx.html.id
import kotlinx.html.js.div
import kotlinx.html.js.onClickFunction
import kotlinx.html.js.table
import kotlinx.html.td
import kotlinx.html.tr
import org.w3c.dom.Element
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLTableElement
import kotlin.browser.document
import kotlin.dom.clear

private const val TREE_SVG_CONTAINER = "tree-svg-container"
private const val TREE_STATS_CONTAINER = "tree-stats-container"
private const val CSS_BUTTONS = "ui-tree-buttons"
private const val CSS_STATS = "ui-tree-stats"

class TreeView(val viewModel: InspectorViewModel) : HtmlView() {

    override lateinit var element: HTMLElement
    private val tidyTree = TidyTree()
    private val treeElement by lazy { element.requireElementById<HTMLElement>(TREE_SVG_CONTAINER) }
    private val statsElement by lazy { element.requireElementById<HTMLElement>(TREE_STATS_CONTAINER) }
    private var tidyTreeConfig = TreeConfig.defaultTidyTree
        set(value) {
            if (field != value) {
                field = value
                drawDiagram()
            }
        }

    override fun buildContent() {
        element = document.create.div {
            div(classes = CSS_BUTTONS) {
                button {
                    text("Default")
                    onClickFunction = { tidyTreeConfig = TreeConfig.defaultTidyTree }
                }
                button {
                    text("ShortType")
                    onClickFunction = { tidyTreeConfig = TreeConfig.shortTypesTidyTree }
                }
                button {
                    text("Simple")
                    onClickFunction = { tidyTreeConfig = TreeConfig.verticalSimpleTree }
                }
            }
            div(classes = CSS_STATS) {
                id = TREE_STATS_CONTAINER
            }
            div {
                id = TREE_SVG_CONTAINER
            }
        }
    }

    override fun onAttachToRoot(rootElement: Element) {
        super.onAttachToRoot(rootElement)
        viewModel.rootNode.observe {
            it?.let { drawDiagram() }
        }
    }

    private fun drawDiagram() {
        viewModel.rootNode.item?.let {
            treeElement.clear()
            statsElement.clear()
            treeElement.append(tidyTree.generateSvg(it, tidyTreeConfig))
            statsElement.append(statsTable(it))
        }
    }

    private fun statsTable(node: ViewNode): HTMLTableElement {
        val nodes = node.all()
        val itemsSimple = nodes.groupBy { it.typeSimple }
        val itemsFull = nodes.groupBy { it.type }
        //check if we can use Simple Names (there is no same View with different package)
        val items = (if (itemsSimple.size == itemsFull.size) itemsSimple else itemsFull)
            .mapValues { it.value.size }
            .toList()
            .sortedByDescending { it.second }
            .toList()

        return document.create.table {
            items.forEach { (type, count) ->
                tr {
                    td { text(type) }
                    td { text(count.toString()) }
                }
            }
            tr {
                td { text("----- SUM ------") }
                td { text(nodes.size.toString()) }
            }
        }
    }
}