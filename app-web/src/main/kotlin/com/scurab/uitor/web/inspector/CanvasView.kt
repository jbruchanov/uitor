package com.scurab.uitor.web.inspector

import com.scurab.uitor.common.render.Color
import com.scurab.uitor.common.render.RectangleRenderContext
import com.scurab.uitor.common.render.StrokeRenderContext
import com.scurab.uitor.common.render.relativeToScale
import com.scurab.uitor.common.render.toColor
import com.scurab.uitor.common.util.dlog
import com.scurab.uitor.common.util.ref
import com.scurab.uitor.web.common.MOUSE_MIDDLE
import com.scurab.uitor.web.common.addMouseClickListener
import com.scurab.uitor.web.common.addMouseMoveListener
import com.scurab.uitor.web.common.addMouseOutListener
import com.scurab.uitor.web.common.addMouseWheelListener
import com.scurab.uitor.web.model.ViewNode
import com.scurab.uitor.web.model.toggleIgnored
import com.scurab.uitor.web.ui.HtmlView
import com.scurab.uitor.web.util.LoadImageHandler
import com.scurab.uitor.web.util.clear
import com.scurab.uitor.web.util.drawCross
import com.scurab.uitor.web.util.drawRectangle
import com.scurab.uitor.web.util.pickNodeForNotification
import kotlinx.html.canvas
import kotlinx.html.div
import org.khronos.webgl.get
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.Image
import org.w3c.dom.events.MouseEvent
import kotlinx.browser.window
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sign

private const val CSS_CANVAS_CONTAINER = "canvas-container"
private const val LAYER_IMAGE = 0
private const val LAYER_DRAWING = 1
private const val SCALE_STEP = 0.05
private const val SCALE_MAX = 3.0
private const val SCALE_MIN = 0.1

private typealias Point = Pair<Double, Double>

private const val HEIGHT_OFFSET = 45.0//status bar
class CanvasView(
    private val inspectorViewModel: InspectorViewModel
) : HtmlView() {

    override var element: HTMLElement? = null
    private val TAG = "CanvasView"
    private var layers: Array<HTMLCanvasElement> = Array(2) { document.create.canvas(null, "") as HTMLCanvasElement }
    private val image = Image()
    private val imageContext get() = layers[LAYER_IMAGE].context
    private val drawingContext get() = layers[LAYER_DRAWING].context
    private val mouseCrossRender = StrokeRenderContext(inspectorViewModel.clientConfig.selectionColor.toColor())
    var nodeRender =
        RectangleRenderContext(
            inspectorViewModel.clientConfig.selectionColor.toColor(),
            inspectorViewModel.clientConfig.selectionColor.toColor().withAlpha(0.3)
        )

    var nodeRenderDiffArea = RectangleRenderContext(Color.Yellow, Color.Yellow.withAlpha(0.05))
    var scale: Double = 1.0;private set
    var renderMouseCross: Boolean = false
    var useWheelToScale: Boolean = false
    var onMouseMove: ((Pair<Double, Double>?, ViewNode?) -> Unit)? = null
    val imageSize get() = Pair(image.width.toDouble(), image.height.toDouble())
    val imageSizeScaled get() = calculateScaleToFit().let { Pair(image.width * it, image.height * it) }

    override fun buildContent() {
        val el = document.create.div(classes = CSS_CANVAS_CONTAINER)
        element = el
        layers.forEach {
            el.append(it)
            it.style.apply {
                position = "fixed"
            }
        }
    }

    override fun onAttached() {
        super.onAttached()
        layers.last().apply {
            addMouseMoveListener {
                mouseMoveAction(it.offsetPoint)
            }
            addMouseOutListener {
                inspectorViewModel.hoveredNode.post(null)
                onMouseMove?.invoke(null, null)
            }
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
            addMouseClickListener(MOUSE_MIDDLE) {
                it.offsetPoint.viewNode()?.let { vn ->
                    val ignored = vn.toggleIgnored(inspectorViewModel.ignoringViewNodeIdsOrPositions)
                    inspectorViewModel.ignoredViewNodeChanged.post(Pair(vn, ignored))
                    //simulate like we moved => triggers all the events again to refresh listeners
                    mouseMoveAction(it.offsetPoint)
                }
            }
        }
        document.addKeyDownListener { keyboardEvent ->
            dlog(TAG) { "KeyEvent:${keyboardEvent.keyCode} => '${keyboardEvent.key}'(${keyboardEvent.code})" }
            when (keyboardEvent.keyCode) {
                106/*'*'*/ -> renderDeviceScreenshot(calculateScaleToFit())
                107/*+*/ -> onScaleChange(1)
                109/*-*/ -> onScaleChange(-1)
                else -> {/*none*/
                }
            }
        }

        document.addWindowResizeListener {
            renderDeviceScreenshot(calculateScaleToFit())
        }

        inspectorViewModel.selectedNode.observe {
            renderScene(it)
        }
        inspectorViewModel.hoveredNode.observe {
            renderScene(inspectorViewModel.selectedNode.item ?: it)
        }
    }

    private val mouseMoveAction = { offsetPoint: Pair<Double, Double> ->
        val viewNode = offsetPoint.viewNode()
        inspectorViewModel.hoveredNode.post(viewNode)
        onMouseMove?.invoke(offsetPoint, viewNode)
        renderMouseCross { offsetPoint }
    }

    private var loadImageHandler: LoadImageHandler? = null
    suspend fun loadImage(url: String) {
        loadImageHandler?.cancel()
        loadImageHandler = LoadImageHandler(image).apply {
            val image = load(url)
            dlog(TAG) { "Image loaded, url:${url} size:${image.width}x${image.height}" }
            renderDeviceScreenshot(calculateScaleToFit())
        }
    }

    //region scale
    fun scaleToFit() {
        renderDeviceScreenshot(calculateScaleToFit())
    }

    private fun calculateScaleToFit(): Double {
        val imageWidth = image.width
        val imageHeight = image.height
        val windowWidth = window.innerWidth
        val windowHeight = window.innerHeight
        val maxH = windowHeight - HEIGHT_OFFSET
        val maxW = min(element.ref.getBoundingClientRect().width, windowWidth - 1000.0)
        val scaleW = max(SCALE_MIN / 100, maxW / imageWidth)
        val scaleH = max(SCALE_MIN / 100, maxH / imageHeight)
        return min(scaleW, scaleH)
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
                drawingContext.drawRectangle(renderArea, nodeRenderDiffArea)
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
            second.relativeToScale(scale).roundToInt(),
            inspectorViewModel.ignoringViewNodeIdsOrPositions
        )
    }

    fun getColor(coords: Pair<Double, Double>): Color {
        val (x, y) = coords
        val arr = imageContext.getImageData(x, y, 1.0, 1.0).data
        return Color.fromBytes(arr[3], arr[0], arr[1], arr[2])
    }
}