package com.scurab.uitor.web.inspector

import com.scurab.uitor.common.model.IViewNode
import com.scurab.uitor.common.render.RectangleRenderContext
import com.scurab.uitor.common.render.StrokeRenderContext
import com.scurab.uitor.common.render.relativeToScale
import com.scurab.uitor.common.render.toColor
import com.scurab.uitor.common.util.dlog
import com.scurab.uitor.web.*
import com.scurab.uitor.web.util.LoadImageHandler
import kotlinx.html.canvas
import kotlinx.html.dom.create
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.Element
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.Image
import org.w3c.dom.events.EventListener
import org.w3c.dom.events.KeyboardEvent
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
private const val SCALE_MIN = 0.5

class CanvasView(
    private val rootElement: Element
) {
    private val TAG = "CanvasView"
    private var layers: Array<HTMLCanvasElement> = Array(2) { document.create.canvas(null, "") as HTMLCanvasElement }
    private val image = Image()
    private val imageContext get() = layers[LAYER_IMAGE].context
    private val drawingContext get() = layers[LAYER_DRAWING].context
    private val mouseCrossRender = StrokeRenderContext("#8F00".toColor())
    private val nodeRender = RectangleRenderContext("#F00".toColor(), "#3F00".toColor())


    var renderMouseCross: Boolean = false
    var root: IViewNode? = null
    var selectedNode: IViewNode? = null

    private var scale: Double = 1.0

    init {
        document.addEventListener(Events.keydown.name, EventListener {
            val keyboardEvent = it as KeyboardEvent
            dlog(TAG) { "KeyEvent:${keyboardEvent.keyCode} => '${keyboardEvent.key}'(${keyboardEvent.code})" }
            when (keyboardEvent.keyCode) {
                106/*'*'*/ -> renderImage(scaleToFit())
                107/*+*/ -> onScaleChange(1)
                109/*-*/ -> onScaleChange(-1)
                else -> {/*none*/}
            }
        })

        layers.forEach {
            it.style.apply {
                cursor = CURSOR_CROSS_HAIR
                position = "fixed"
            }
            rootElement.append(it)
        }

        layers.last().apply {
            addMouseMoveListener { onMouseMove(it.offsetX, it.offsetY) }
            addMouseOutListener { onMouseLeave() }
            addMouseWheelListener { onScaleChange(sign(-it.deltaY).toInt()) }
            addMouseClickListener { onMouseClick(it.offsetX, it.offsetY) }
        }
    }

    private fun scaleToFit(): Double {
        //TODO:landscape
        return (window.innerHeight.toDouble() - INNER_HEIGHT_OFFSET) / image.height
    }

    private fun onMouseMove(x: Double, y: Double) {
        renderScene(x, y)
    }

    private fun onMouseClick(x: Double, y: Double) {
        val found = root?.findFrontVisibleView(
            x.relativeToScale(this.scale).roundToInt(),
            y.relativeToScale(this.scale).roundToInt()
        )
        selectedNode = when {
            selectedNode != null && found == selectedNode -> null
            else -> found
        }
        dlog(TAG) {
            "mouseClick: X:${x.roundToInt()}, Y:${y.roundToInt()} selectedNode:${selectedNode?.position ?: "null"}"
        }
        renderScene(x, y)
    }

    private fun onMouseLeave() {
        dlog(TAG) { "onMouseLeave" }
        drawingContext.clear()
        //just keep rendered selected node if necessary
        renderViewRectangle(-1.0, -1.0)
    }

    private fun onScaleChange(sign: Int) {
        renderImage(this.scale + (sign * SCALE_STEP))
    }

    private fun updateScale(scale: Double = this.scale) {
        val newScale = maxOf(minOf(scale, SCALE_MAX), SCALE_MIN)
        this.scale = newScale
        layers.forEach {
            it.width = (image.width * newScale).roundToInt()
            it.height = (image.height * newScale).roundToInt()
        }
    }

    private var loadImageHandler: LoadImageHandler? = null
    suspend fun loadImage(url: String) {
        loadImageHandler?.cancel()
        loadImageHandler = LoadImageHandler(image).apply {
            val image = load(url)
            dlog(TAG) { "Image loaded, url:${url} size:${image.width}x${image.height}" }
            renderImage(scaleToFit())
        }
    }

    //region rendering
    private fun renderScene(x: Double, y: Double) {
        drawingContext.clear()
        renderViewRectangle(x, y)
        renderMouseCross(x, y)
    }

    private fun renderViewRectangle(x: Double, y: Double) {
        //take the selected or find by mouse
        val view = selectedNode ?: root?.findFrontVisibleView(
            x.relativeToScale(this.scale).roundToInt(),
            y.relativeToScale(this.scale).roundToInt()
        )
        dlog(TAG) {
            "renderViewRectangle: [${x.roundToInt()},${y.roundToInt()}]" +
                    " ViewPosition:${view?.position ?: ""}" +
                    " ViewId:${view?.ids}"
        }
        view?.let {
            drawingContext.drawRectangle(it.rect.scale(this.scale), nodeRender)
        }
    }

    private fun renderMouseCross(x: Double, y: Double) {
        if (renderMouseCross) {
            drawingContext.drawCross(x, y, mouseCrossRender)
        }
    }

    private fun renderImage(scale: Double = this.scale) {
        val newScale = maxOf(minOf(scale, 3.0), 0.1)
        this.scale = newScale
        val width = (image.width * this.scale)
        val height = (image.height * this.scale)
        layers.forEach {
            it.width = width.roundToInt()
            it.height = height.roundToInt()
        }
        imageContext.drawImage(image, 0.0, 0.0, width, height)
    }
    //endregion rendering

    val HTMLCanvasElement.context get() = getContext("2d") as CanvasRenderingContext2D
}