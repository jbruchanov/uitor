package com.scurab.uitor.web.tree

import com.scurab.uitor.common.util.ref
import com.scurab.uitor.web.common.InspectorPage
import com.scurab.uitor.web.inspector.InspectorViewModel
import com.scurab.uitor.web.model.PageViewModel
import com.scurab.uitor.web.model.ViewNode
import com.scurab.uitor.web.util.HashToken
import com.scurab.uitor.web.util.lazyLifecycled
import com.scurab.uitor.web.util.requireElementById
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.html.button
import kotlinx.html.div
import kotlinx.html.id
import kotlinx.html.js.div
import kotlinx.html.js.onClickFunction
import kotlinx.html.js.table
import kotlinx.html.td
import kotlinx.html.tr
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLTableElement
import kotlin.browser.window
import kotlin.dom.clear

private const val TREE_SVG_CONTAINER = "tree-svg-container"
private const val TREE_STATS_CONTAINER = "tree-stats-container"
private const val CSS_BUTTONS = "ui-tree-buttons"
private const val CSS_STATS = "ui-tree-stats"
private const val CONFIG_DEFAULT = "default"
private const val CONFIG_SHORT = "short"
private const val CONFIG_SIMPLE = "simple"

class TidyTreePage(pageViewModel: PageViewModel) : InspectorPage(InspectorViewModel(pageViewModel)) {

    override var element: HTMLElement? = null; private set
    private val tidyTree = TidyTree()
    private val treeElement by lazyLifecycled { element.ref.requireElementById<HTMLElement>(TREE_SVG_CONTAINER) }
    private val statsElement by lazyLifecycled { element.ref.requireElementById<HTMLElement>(TREE_STATS_CONTAINER) }
    private val configs = mapOf(
        CONFIG_DEFAULT to TreeConfig.defaultTidyTree,
        CONFIG_SHORT to TreeConfig.shortTypesTidyTree,
        CONFIG_SIMPLE to TreeConfig.verticalSimpleTree
    )
    private var tidyTreeConfig = configFromLocationHash()

    init {
        GlobalScope.launch {
            try {
                viewModel.load()
            } catch (e: Exception) {
                window.alert(e.message ?: "Null message")
            }
        }
    }

    override fun buildContent() {
        element = document.create.div {
            div(classes = CSS_BUTTONS) {
                button {
                    text("Default")
                    onClickFunction = { setConfigHash(CONFIG_DEFAULT) }
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
        window.location.hash = HashToken(window.location.hash).append("type", key).toString()
        setTreeConfig(configs[key] ?: TreeConfig.defaultTidyTree)
    }

    private fun setTreeConfig(treeConfig: TreeConfig) {
        tidyTreeConfig = treeConfig
        drawDiagram(false)
    }

    override fun onAttached() {
        super.onAttached()
        viewModel.rootNode.observe {
            it?.let { drawDiagram(true) }
        }
    }

    override fun stateDescription(): String {
        return HashToken.state(
            HashToken.SCREEN_INDEX to viewModel.screenIndex.toString(),
            HashToken.TYPE to (configs.entries.find { it.value == tidyTreeConfig }?.key ?: CONFIG_DEFAULT)
        )
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

    private fun configFromLocationHash(hashToken: HashToken = HashToken(window.location.hash)): TreeConfig {
        return when (hashToken.type) {
            CONFIG_SHORT -> TreeConfig.shortTypesTidyTree
            CONFIG_SIMPLE -> TreeConfig.verticalSimpleTree
            else -> TreeConfig.defaultTidyTree
        }
    }

    override fun onHashTokenChanged(old: HashToken, new: HashToken) {
        super.onHashTokenChanged(old, new)
        setTreeConfig(configFromLocationHash(new))
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