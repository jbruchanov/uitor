package com.scurab.uitor.web.tree

import com.scurab.uitor.common.util.ref
import com.scurab.uitor.web.common.BaseViewPropertiesPage
import com.scurab.uitor.web.common.Navigation
import com.scurab.uitor.web.model.PageViewModel
import com.scurab.uitor.web.util.HashToken
import com.scurab.uitor.web.util.lazyLifecycled
import com.scurab.uitor.web.util.requireElementById
import kotlinx.html.button
import kotlinx.html.div
import kotlinx.html.id
import kotlinx.html.js.div
import kotlinx.html.js.onClickFunction
import org.w3c.dom.HTMLElement
import kotlin.browser.window
import kotlin.dom.clear

private const val CSS_BUTTONS = "ui-tree-buttons"
private const val ID_TIDY_TREE = "ui-tree-tidy-tree"
private const val ID_TREE_CONTAINER = "ui-tree-view-container"

class TidyTreePage(pageViewModel: PageViewModel) : BaseViewPropertiesPage(pageViewModel) {

    override var contentElement: HTMLElement? = null; private set
    private val tidyTree = TidyTree()
    private val container by lazyLifecycled { contentElement.ref.requireElementById<HTMLElement>(ID_TREE_CONTAINER) }
    private val configs = listOf(TreeConfig.defaultTidyTree, TreeConfig.shortTypesTidyTree, TreeConfig.verticalSimpleTree)
    private var tidyTreeConfig = configFromLocationHash()
    private var expandViewPropsColumn = true

    override fun buildContent() {
        super.buildContent()
        contentElement = document.create.div {
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
                id = ID_TREE_CONTAINER
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

    override fun onAttached() {
        super.onAttached()
        viewModel.rootNode.observe {
            it?.let { drawDiagram(true) }
        }
    }

    override fun stateDescription(): String {
        return HashToken.state(
            HashToken.SCREEN_INDEX to viewModel.screenIndex.toString(),
            HashToken.TYPE to tidyTreeConfig.id
        )
    }

    private fun drawDiagram(refreshStats: Boolean) {
        viewModel.rootNode.item?.let { viewNode ->
            container.clear()
            val svgDiagram = tidyTree.generateSvg(viewNode,
                viewModel.selectedNode.item,
                tidyTreeConfig,
                viewModel.clientConfig
            ) {
                viewModel.selectedNode.post(it)
            }
            svgDiagram.id = ID_TIDY_TREE
            container.append(svgDiagram)
        }
    }

    private fun configFromLocationHash(hashToken: HashToken = HashToken(window.location.hash)): TreeConfig {
        return configs.find { it.id == hashToken.type } ?: TreeConfig.defaultTidyTree
    }

    override fun onHashTokenChanged(old: HashToken, new: HashToken) {
        super.onHashTokenChanged(old, new)
        setTreeConfig(configFromLocationHash(new))
    }
}