package com.scurab.uitor.web.inspector

import com.scurab.uitor.common.render.relativeToScale
import com.scurab.uitor.common.util.ise
import com.scurab.uitor.common.util.ref
import com.scurab.uitor.web.common.InspectorPage
import com.scurab.uitor.web.ui.ViewPropsStatsView
import com.scurab.uitor.web.model.PageViewModel
import com.scurab.uitor.web.model.ViewNode
import com.scurab.uitor.web.model.isIgnored
import com.scurab.uitor.web.ui.CSS_PROPERTIES_COLOR
import com.scurab.uitor.web.ui.ColumnsLayout
import com.scurab.uitor.web.ui.IColumnsLayoutDelegate
import com.scurab.uitor.web.ui.launchWithProgressBar
import com.scurab.uitor.web.util.SCROLL_BAR_WIDTH
import com.scurab.uitor.web.util.lazyLifecycled
import com.scurab.uitor.web.util.requireElementById
import kotlinx.html.checkBoxInput
import kotlinx.html.id
import kotlinx.html.js.div
import kotlinx.html.js.onClickFunction
import kotlinx.html.span
import kotlinx.html.table
import kotlinx.html.td
import kotlinx.html.tr
import org.w3c.dom.Element
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.HTMLSpanElement
import org.w3c.dom.HTMLTableElement
import org.w3c.dom.get
import kotlin.browser.window
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

private const val ID_COORDS = "canvas-status-bar-coordinates"
private const val ID_COLOR_NAME = "canvas-status-bar-color-name"
private const val ID_COLOR_PREVIEW = "canvas-status-bar-color-preview"
private const val ID_VIEW_NAME = "canvas-status-view-name"
private const val ID_IGNORE_CHECKBOX = "canvas-status-bar-ignore"
private const val CANVAS_STATUS_BAR = "canvas-status-bar"

class LayoutInspectorPage(
    pageViewModel: PageViewModel
) : InspectorPage(InspectorViewModel(pageViewModel)) {

    private lateinit var columnsLayout: ColumnsLayout
    private lateinit var canvasContainer: HTMLElement
    private lateinit var canvasView: CanvasView
    private lateinit var treeView: TreeView
    private val propertiesView = ViewPropsStatsView(viewModel)
    private val mouseLocation by lazyLifecycled { element.ref.requireElementById<HTMLSpanElement>(ID_COORDS) }
    private val colorName by lazyLifecycled { element.ref.requireElementById<HTMLElement>(ID_COLOR_NAME) }
    private val colorPreview by lazyLifecycled { element.ref.requireElementById<HTMLElement>(ID_COLOR_PREVIEW) }
    private val viewName by lazyLifecycled { element.ref.requireElementById<HTMLSpanElement>(ID_VIEW_NAME) }
    private val ignoreCheckBox by lazyLifecycled { element.ref.requireElementById<HTMLInputElement>(ID_IGNORE_CHECKBOX) }

    override var element: HTMLElement? = null; private set

    override fun buildContent() {
        columnsLayout = ColumnsLayout(ColumnsLayoutDelegate(this))
        canvasContainer = document.create.div {
            table (classes = CANVAS_STATUS_BAR) {
                tr {
                    td { span { id = ID_COORDS } }
                    td { span(classes = CSS_PROPERTIES_COLOR) { id = ID_COLOR_PREVIEW } }
                    td { span { id = ID_COLOR_NAME } }
                    td {
                        text("Pointer ignore:")
                        checkBoxInput {
                            id = ID_IGNORE_CHECKBOX
                            disabled = true
                            onClickFunction = {
                                onSkipClicked((it.target as HTMLInputElement).checked)
                            }
                        }
                    }
                    td { span { id = ID_VIEW_NAME } }
                }
            }
        }
        canvasView = CanvasView(viewModel)
        treeView = TreeView(viewModel)
        propertiesView.buildContent()
    }

    override fun onAttachToRoot(rootElement: Element) {
        columnsLayout.attachTo(rootElement)
        columnsLayout.left.append(canvasContainer)
        canvasView.attachTo(canvasContainer)
        treeView.attachTo(columnsLayout.middle.first())
        propertiesView.attachTo(columnsLayout.right)
        element = columnsLayout.element

        canvasView.onMouseMove = { mouse, viewNode ->
            updateStatusBar(mouse, viewModel.selectedNode.item ?: viewNode)
        }
        updateStatusBar(null, null)
    }

    private fun updateStatusBar(mouse: Pair<Double, Double>?, viewNode: ViewNode?) {
        val x = mouse?.first?.relativeToScale(canvasView.scale)?.roundToInt()
        val y = mouse?.second?.relativeToScale(canvasView.scale)?.roundToInt()
        val color = mouse?.let { canvasView.getColor(it) }
        mouseLocation.innerText = "XY:" + (mouse?.let { "[$x,$y]" } ?: "")
        colorPreview.style.backgroundColor = color?.htmlRGB ?: ""
        colorName.innerText = (color?.htmlRGB ?: "")
        viewName.innerText = "ID:" + (viewNode?.ids ?: "")
        ignoreCheckBox.disabled = viewModel.selectedNode.item == null
        ignoreCheckBox.checked = viewNode?.isIgnored(viewModel.ignoringViewNodeIdsOrPositions) ?: false
    }

    private fun onSkipClicked(value: Boolean) {
        val item = viewModel.selectedNode.item ?: ise("Skip checkbox is clicked and no ViewNode selected?!")
        if (value) {
            viewModel.ignoringViewNodeIdsOrPositions.add(item.position)
        } else {
            viewModel.ignoringViewNodeIdsOrPositions.remove(item.position)
        }
        viewModel.ignoredViewNodeChanged.post(Pair(item, value))
    }

    override fun onAttached() {
        super.onAttached()
        canvasView.element.ref.hidden = true
        launchWithProgressBar {
            canvasView.loadImage(viewModel.screenPreviewUrl)
            canvasView.element.ref.hidden = false
            canvasView.scaleToFit()
        }
        viewModel.apply {
            rootNode.observe {
                canvasView.renderMouseCross = true
                columnsLayout.initColumnSizes()
            }

            selectedNode.observe {vn ->
                updateStatusBar(null, vn)
            }

            hoveredNode.observe {
                updateStatusBar(null, it ?: viewModel.selectedNode.item)
            }
            ignoredViewNodeChanged.observe {
                updateStatusBar(null, viewModel.selectedNode.item ?: it.first)
            }
        }
    }

    override fun onDetached() {
        canvasView.detach()
        treeView.detach()
        propertiesView.detach()
        super.onDetached()
    }

    class ColumnsLayoutDelegate(val page: LayoutInspectorPage) : IColumnsLayoutDelegate {
        override val innerContentWidthEstimator: (Int) -> Double = { column ->
            if (window.innerWidth < 1280) {
                window.innerWidth / 3.0
            } else {
                when (column) {
                    2 -> {
                        SCROLL_BAR_WIDTH + ((page.treeView.element as? HTMLTableElement)?.rows?.get(0)?.getBoundingClientRect()?.width
                            ?: window.innerWidth / 4.0)
                    }
                    else -> max(500.0, min(window.innerWidth, window.screen.width) / 3.0)
                }
            }
        }
    }
}