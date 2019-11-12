package com.scurab.uitor.web.inspector

import com.scurab.uitor.common.render.relativeToScale
import com.scurab.uitor.common.util.dlog
import com.scurab.uitor.common.util.ise
import com.scurab.uitor.common.util.ref
import com.scurab.uitor.web.common.InspectorPage
import com.scurab.uitor.web.ui.viewproperties.ViewPropsStatsView
import com.scurab.uitor.web.model.PageViewModel
import com.scurab.uitor.web.model.ViewNode
import com.scurab.uitor.web.model.isIgnored
import com.scurab.uitor.web.ui.viewproperties.CSS_PROPERTIES_COLOR
import com.scurab.uitor.web.ui.ColumnsLayout
import com.scurab.uitor.web.ui.IColumnsLayoutDelegate
import com.scurab.uitor.web.ui.launchWithProgressBar
import com.scurab.uitor.web.util.SCROLL_BAR_WIDTH
import com.scurab.uitor.web.util.lazyLifecycled
import com.scurab.uitor.web.util.requireElementById
import kotlinx.html.*
import kotlinx.html.js.div
import kotlinx.html.js.onClickFunction
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
    private val columnsLayoutDelegate = ColumnsLayoutDelegate(this)

    override var element: HTMLElement? = null; private set

    override fun buildContent() {
        columnsLayout = ColumnsLayout(columnsLayoutDelegate)
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
            reInitColumns(initColumnsState + 1)
        }
        viewModel.apply {
            rootNode.observe {
                canvasView.renderMouseCross = true
                reInitColumns(initColumnsState + 1)
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

        document.addWindowResizeListener {
            //not the most amazing UX for downsizing
            reInitColumns(initColumnsState)
        }
    }

    private var initColumnsState = 0
    private fun reInitColumns(state: Int) {
        initColumnsState = state
        //workaround for the middle column
        //not sure how to get the max width to fill the space, getBoundingClientRect gives range of values
        //between "row is min width to not show scrollbar" to "not filling space anymore"
        //so let's make it almost full screen and then it gives the value to fill the width as expected
        if(state == 2) {
            val expectedCanvasWidth = canvasView.imageSizeScaled.first
            columnsLayout.setGridTemplateColumns("10px 5px 1fr 5px 10px")
            val expectedMiddleColumnWidth =
                (treeView.element as? HTMLTableElement)?.rows?.get(0)?.getBoundingClientRect()?.width ?: 0.0
            columnsLayoutDelegate.recalculate(expectedCanvasWidth, expectedMiddleColumnWidth)
            columnsLayout.initColumnSizes()
        }
    }

    override fun onDetached() {
        canvasView.detach()
        treeView.detach()
        propertiesView.detach()
        super.onDetached()
    }

    class ColumnsLayoutDelegate(val page: LayoutInspectorPage) : IColumnsLayoutDelegate {
        private var col1 = 0.0
        private var col2 = 0.0
        private var col3 = 0.0
        private val minCol3 = 500.0
        private val expCol3 = max(minCol3, min(window.innerWidth, window.screen.width) / 3.0)

        fun recalculate(imageWidth: Double, treeWidth: Double) {
            val resWidth = window.innerWidth - imageWidth
            col1 = imageWidth
            when {
                resWidth > (treeWidth + expCol3) -> {
                    col2 = treeWidth
                    col3 = expCol3
                }
                resWidth > (treeWidth + minCol3) -> {
                    col2 = treeWidth
                    col3 = minCol3
                }
                else -> {
                    col2 = resWidth / 2
                    col3 = resWidth / 2
                }
            }
        }

        override val innerContentWidthEstimator: (Int) -> Double = { column ->
            if (window.innerWidth < 1280) {
                window.innerWidth / 3.0
            } else {
                when (column) {
                    0 -> col1
                    2 -> col2
                    4 -> col3
                    else -> 0.0
                }
            }
        }
    }
}