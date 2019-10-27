package com.scurab.uitor.web.inspector

import com.scurab.uitor.common.render.Color
import com.scurab.uitor.common.render.RectangleRenderContext
import com.scurab.uitor.common.render.StrokeRenderContext
import com.scurab.uitor.common.render.relativeToScale
import com.scurab.uitor.common.render.toColor
import com.scurab.uitor.common.util.dlog
import com.scurab.uitor.web.Events
import com.scurab.uitor.web.addMouseClickListener
import com.scurab.uitor.web.addMouseMoveListener
import com.scurab.uitor.web.addMouseOutListener
import com.scurab.uitor.web.addMouseWheelListener
import com.scurab.uitor.web.clear
import com.scurab.uitor.web.drawCross
import com.scurab.uitor.web.drawRectangle
import com.scurab.uitor.web.model.ViewNode
import com.scurab.uitor.web.ui.HtmlView
import com.scurab.uitor.web.util.LoadImageHandler
import com.scurab.uitor.web.util.pickNodeForNotification
import kotlinx.html.canvas
import kotlinx.html.dom.create
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.Element
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.Image
import org.w3c.dom.events.EventListener
import org.w3c.dom.events.KeyboardEvent
import org.w3c.dom.events.MouseEvent
import kotlin.browser.document
import kotlin.browser.window
import kotlin.math.roundToInt
import kotlin.math.sign

private const val LAYER_IMAGE = 0
private const val LAYER_DRAWING = 1
private const val CURSOR_CROSS_HAIR = "crosshair"
private const val INNER_HEIGHT_OFFSET = 50
private const val SCALE_STEP = 0.05
private const val SCALE_MAX = 3.0
private const val SCALE_MIN = 0.1

private typealias Point = Pair<Double, Double>

class CanvasView(
    private val inspectorViewModel: InspectorViewModel
) : HtmlView() {

    //TODO: fix to have it inside element
    override var element: HTMLElement? = null

    private val TAG = "CanvasView"
    private var layers: Array<HTMLCanvasElement> = Array(2) { document.create.canvas(null, "") as HTMLCanvasElement }
    private val image = Image()
    private val imageContext get() = layers[LAYER_IMAGE].context
    private val drawingContext get() = layers[LAYER_DRAWING].context
    private val mouseCrossRender = StrokeRenderContext(inspectorViewModel.clientConfig.selectionColor.toColor())
    private val nodeRender =
        RectangleRenderContext(
            inspectorViewModel.clientConfig.selectionColor.toColor(),
            inspectorViewModel.clientConfig.selectionColor.toColor().withAlpha(0.3)
        )
    private val nodeDifferentAreaRender =
        RectangleRenderContext(
            Color.Yellow,
            Color.Yellow.withAlpha(0.05)
        )
    private var scale: Double = 1.0

    var renderMouseCross: Boolean = false
    var useWheelToScale: Boolean = false

    override fun onAttachToRoot(rootElement: Element) {
        layers.forEach { rootElement.append(it) }
    }

    override fun buildContent() {
        layers.forEach {
            it.style.apply {
                cursor = CURSOR_CROSS_HAIR
                position = "fixed"
            }
        }

        layers.last().apply {
            addMouseMoveListener {
                inspectorViewModel.hoveredNode.post(it.offsetPoint.viewNode())
                renderMouseCross { it.offsetPoint }
            }
            addMouseOutListener { inspectorViewModel.hoveredNode.post(null) }
            addMouseWheelListener {
                if (useWheelToScale) {
                    onScaleChange(sign(-it.deltaY).toInt())
                }
            }
            addMouseClickListener {
                inspectorViewModel.selectedNode.apply {
                    val node = pickNodeForNotification(item, it.offsetPoint.viewNode())
                    post(node)
                }
                renderScene(it.offsetPoint)
            }
        }
    }

    override fun onAttached() {
        super.onAttached()
        document.addEventListener(Events.keydown.name, EventListener {
            val keyboardEvent = it as KeyboardEvent
            dlog(TAG) { "KeyEvent:${keyboardEvent.keyCode} => '${keyboardEvent.key}'(${keyboardEvent.code})" }
            when (keyboardEvent.keyCode) {
                106/*'*'*/ -> renderDeviceScreenshot(scaleToFit())
                107/*+*/ -> onScaleChange(1)
                109/*-*/ -> onScaleChange(-1)
                else -> {/*none*/
                }
            }
        })

        inspectorViewModel.selectedNode.observe {
            renderScene(it)
        }
        inspectorViewModel.hoveredNode.observe {
            renderScene(inspectorViewModel.selectedNode.item ?: it)
        }
    }

    private var loadImageHandler: LoadImageHandler? = null
    suspend fun loadImage(url: String) {
        loadImageHandler?.cancel()
        loadImageHandler = LoadImageHandler(image).apply {
            val image = load(url)
            dlog(TAG) { "Image loaded, url:${url} size:${image.width}x${image.height}" }
            renderDeviceScreenshot(scaleToFit())
        }
    }

    //region scale
    private fun scaleToFit(): Double {
        //TODO:landscape
        return (window.innerHeight.toDouble() - INNER_HEIGHT_OFFSET) / image.height
    }

    private fun onScaleChange(sign: Int) {
        renderDeviceScreenshot(this.scale + (sign * SCALE_STEP))
        renderScene()
    }
    //endregion scale

    //region rendering
    private fun renderScene(point: Point) {
        drawingContext.clear()
        renderViewRectangle(point)
        renderMouseCross { point }
    }

    private fun renderScene(viewNode: ViewNode? = inspectorViewModel.selectedNode.item) {
        drawingContext.clear()
        renderViewRectangle(viewNode)
    }

    private fun renderViewRectangle(point: Point) {
        val view = point.viewNode()
        dlog(TAG) {
            "renderViewRectangle: [${point.first.roundToInt()},${point.second.roundToInt()}]" +
                    " ViewPosition:${view?.position ?: ""}" +
                    " ViewId:${view?.ids}"
        }
        renderViewRectangle(view)
    }

    private fun renderViewRectangle(viewNode: ViewNode?) {
        viewNode?.let {
            val rect = it.rect
            val view = rect.scale(scale)
            drawingContext.drawRectangle(view, nodeRender)
            it.renderAreaRelative?.let { render ->
                val renderArea = rect.addRelative(render).scale(scale)
                drawingContext.drawRectangle(renderArea, nodeDifferentAreaRender)
            }
        }
    }

    /**
     * Render MouseCross if enabled
     */
    private inline fun renderMouseCross(point: () -> Point) {
        if (renderMouseCross) {
            val p = point()
            drawingContext.drawCross(p.first, p.second, mouseCrossRender)
        }
    }

    @Suppress("NAME_SHADOWING")
    private fun renderDeviceScreenshot(newScale: Double = this.scale) {
        val newScale = maxOf(minOf(newScale, SCALE_MAX), SCALE_MIN)
        scale = newScale
        val width = (image.width * scale)
        val height = (image.height * scale)
        layers.forEach {
            it.width = width.roundToInt()
            it.height = height.roundToInt()
        }
        imageContext.drawImage(image, 0.0, 0.0, width, height)
    }
    //endregion rendering

    private val HTMLCanvasElement.context get() = getContext("2d") as CanvasRenderingContext2D
    private val MouseEvent.offsetPoint: Point get() = Pair(offsetX, offsetY)
    private fun Point.viewNode(): ViewNode? {
        return inspectorViewModel.rootNode.item?.findFrontVisibleView(
            first.relativeToScale(scale).roundToInt(),
            second.relativeToScale(scale).roundToInt()
        )
    }
}