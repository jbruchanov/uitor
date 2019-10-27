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
import org.w3c.dom.events.Event
import kotlin.browser.document
import kotlin.browser.window
import kotlin.dom.clear

private const val TREE_SVG_CONTAINER = "tree-svg-container"
private const val TREE_STATS_CONTAINER = "tree-stats-container"
private const val CSS_BUTTONS = "ui-tree-buttons"
private const val CSS_STATS = "ui-tree-stats"
private const val CONFIG_SHORT = "short"
private const val CONFIG_SIMPLE = "simple"

class TreeView(private val viewModel: InspectorViewModel) : HtmlView() {

    override lateinit var element: HTMLElement
    private val tidyTree = TidyTree()
    private val treeElement by lazy { element.requireElementById<HTMLElement>(TREE_SVG_CONTAINER) }
    private val statsElement by lazy { element.requireElementById<HTMLElement>(TREE_STATS_CONTAINER) }
    private var tidyTreeConfig = configFromLocationHash()

    override fun buildContent() {
        element = document.create.div {
            div(classes = CSS_BUTTONS) {
                button {
                    text("Default")
                    onClickFunction = { setConfigHash("") }
                }
                button {
                    text("ShortType")
                    onClickFunction = { setConfigHash(CONFIG_SHORT) }
                }
                button {
                    text("Simple")
                    onClickFunction = { setConfigHash(CONFIG_SIMPLE) }
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

    private fun setConfigHash(key: String) {
        window.location.hash = key
    }

    override fun onAttachToRoot(rootElement: Element) {
        super.onAttachToRoot(rootElement)
        viewModel.rootNode.observe {
            it?.let { drawDiagram(true) }
        }
        window.addEventListener("hashchange", { e: Event ->
            tidyTreeConfig = configFromLocationHash()
            drawDiagram(false)
        })
    }

    private fun drawDiagram(refreshStats: Boolean) {
        viewModel.rootNode.item?.let {
            treeElement.clear()
            treeElement.append(tidyTree.generateSvg(it, tidyTreeConfig))
            if (refreshStats) {
                statsElement.clear()
                statsElement.append(statsTable(it))
            }
        }
    }

    private fun configFromLocationHash(): TreeConfig {
        return when(window.location.hash.substringAfter("#")) {
            CONFIG_SHORT -> TreeConfig.shortTypesTidyTree
            CONFIG_SIMPLE -> TreeConfig.verticalSimpleTree
            else -> TreeConfig.defaultTidyTree
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