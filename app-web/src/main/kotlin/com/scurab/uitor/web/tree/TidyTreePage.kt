package com.scurab.uitor.web.tree

import com.scurab.uitor.common.util.ref
import com.scurab.uitor.web.common.InspectorPage
import com.scurab.uitor.web.common.Navigation
import com.scurab.uitor.web.common.ViewPropertiesTableView
import com.scurab.uitor.web.inspector.InspectorViewModel
import com.scurab.uitor.web.model.PageViewModel
import com.scurab.uitor.web.model.ViewNode
import com.scurab.uitor.web.ui.ColumnsLayout
import com.scurab.uitor.web.ui.IColumnsLayoutDelegate
import com.scurab.uitor.web.ui.table.TableViewDelegate
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
import org.w3c.dom.Element
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLTableElement
import kotlin.browser.window
import kotlin.dom.clear
import kotlin.math.max

private const val CSS_BUTTONS = "ui-tree-buttons"
private const val CSS_STATS = "ui-tree-stats"
private const val ID_TIDY_TREE = "ui-tree-tidy-tree"
private const val ID_VIEW_PROPS_CONTAINER = "ui-tree-view-properties"

class TidyTreePage(pageViewModel: PageViewModel) : InspectorPage(InspectorViewModel(pageViewModel)) {

    override var element: HTMLElement? = null; private set
    private val tidyTree = TidyTree()
    private val container by lazyLifecycled { element.ref.requireElementById<HTMLElement>(ID_VIEW_PROPS_CONTAINER) }
    private val configs =
        listOf(TreeConfig.defaultTidyTree, TreeConfig.shortTypesTidyTree, TreeConfig.verticalSimpleTree)
    private var tidyTreeConfig = configFromLocationHash()
    private var viewPropertiesTableView =
        ViewPropertiesTableView(TableViewDelegate.default(viewModel.clientConfig), viewModel.screenIndex)
    private val columnsLayoutDelegate = object : IColumnsLayoutDelegate {
        override val innerContentWidthEstimator: (Int) -> Double = { column ->
            val windowWidth = window.innerWidth - ColumnsLayout.UNKNOWNGAP
            val w = if (viewModel.selectedNode.item != null) max(windowWidth / 3.0, 600.0) else 0.0
            if (column == 0) windowWidth- w else w
        }
    }
    private val columnsLayout = ColumnsLayout(columnsLayoutDelegate, 2)
    private var expandViewPropsColumn = true

    init {
        GlobalScope.launch {
            try {
                viewModel.load()
            } catch (e: Exception) {
                alert(e)
            }
        }
    }

    override fun buildContent() {
        element = document.create.div {
            div(classes = CSS_BUTTONS) {
                button {
                    text("Default")
                    onClickFunction = { setTreeConfig(TreeConfig.defaultTidyTree) }
                }
                button {
                    text("ShortType")
                    onClickFunction = { setTreeConfig(TreeConfig.shortTypesTidyTree) }
                }
                button {
                    text("Simple")
                    onClickFunction = { setTreeConfig(TreeConfig.verticalSimpleTree) }
                }
            }
            div {
                id = ID_VIEW_PROPS_CONTAINER
            }
        }
    }

    private fun setTreeConfig(treeConfig: TreeConfig) {
        if (treeConfig != tidyTreeConfig) {
            expandViewPropsColumn = true
            tidyTreeConfig = treeConfig
            drawDiagram(false)
            Navigation.updateDescriptionState(this)

        }
    }

    override fun onAttachToRoot(rootElement: Element) {
        super.onAttachToRoot(rootElement)
        columnsLayout.attachTo(container)
        viewPropertiesTableView.attachTo(columnsLayout.right)
    }

    override fun onAttached() {
        super.onAttached()
        viewModel.rootNode.observe {
            it?.let { drawDiagram(true) }
        }
        viewModel.selectedNode.observe {
            viewPropertiesTableView.viewNode = it
            if (expandViewPropsColumn) {
                expandViewPropsColumn = false
                columnsLayout.initColumnSizes()
            }
        }
        columnsLayout.initColumnSizes()
    }

    override fun stateDescription(): String {
        return HashToken.state(
            HashToken.SCREEN_INDEX to viewModel.screenIndex.toString(),
            HashToken.TYPE to tidyTreeConfig.id
        )
    }

    private fun drawDiagram(refreshStats: Boolean) {
        viewModel.rootNode.item?.let {
            columnsLayout.left.clear()
            val svgDiagram = tidyTree.generateSvg(it, viewModel.selectedNode.item, tidyTreeConfig) {
                viewModel.selectedNode.post(it)
            }
            svgDiagram.id = ID_TIDY_TREE
            columnsLayout.left.append(svgDiagram)
            if (refreshStats) {
//                statsElement.clear()
//                statsElement.append(statsTable(it))
            }
        }
    }

    private fun configFromLocationHash(hashToken: HashToken = HashToken(window.location.hash)): TreeConfig {
        return configs.find { it.id == hashToken.type } ?: TreeConfig.defaultTidyTree
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