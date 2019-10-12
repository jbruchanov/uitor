package com.scurab.uitor.web

import com.scurab.uitor.common.render.*
import org.w3c.dom.CanvasRenderingContext2D

fun CanvasRenderingContext2D.clear() {
    clearRect(0.0, 0.0, canvas.width.toDouble(), canvas.height.toDouble());
}

fun CanvasRenderingContext2D.drawRectangle(
    x: Int, y: Int, w: Int, h: Int,
    render: IRectangleRenderContext
) {
    drawRectangle(x.toDouble(), y.toDouble(), w.toDouble(), h.toDouble(), render)
}

fun CanvasRenderingContext2D.drawRectangle(
    rect: Rect,
    render: IRectangleRenderContext
) {
    drawRectangle(rect.left.toDouble(), rect.top.toDouble(), rect.width.toDouble(), rect.height.toDouble(), render)
}

fun CanvasRenderingContext2D.drawRectangle(
    x: Double, y: Double, w: Double, h: Double,
    render: IRectangleRenderContext
) {
    lineWidth = render.strokeWidth
    strokeStyle = render.strokeColor.htmlRGB
    drawHorizontalLine(x, y, w)
    drawHorizontalLine(x, y + h, w)
    drawVerticalLine(x, y, h)
    drawVerticalLine(x + w, y, h)

    globalAlpha = render.fillColor.alpha
    fillStyle = render.fillColor.htmlRGB
    fillRect(x, y, w, h)
    globalAlpha = 1.0
}

fun CanvasRenderingContext2D.drawCross(x: Double, y: Double, render: StrokeRenderContext) {
    drawHorizontalLine(0.0, y, canvas.width.d, render)
    drawVerticalLine(x, 0.0, canvas.height.d, render)
}

fun CanvasRenderingContext2D.drawHorizontalLine(x: Double, y: Double, width: Double, render: StrokeRenderContext) {
    lineWidth = render.strokeWidth
    strokeStyle = render.strokeColor.htmlRGB
    globalAlpha = render.strokeColor.alpha
    setLineDash(render.strokeDash ?: emptyArray())
    drawHorizontalLine(x, y, width)
    globalAlpha = 1.0
}

fun CanvasRenderingContext2D.drawVerticalLine(x: Double, y: Double, height: Double, render: StrokeRenderContext) {
    lineWidth = render.strokeWidth
    strokeStyle = render.strokeColor.htmlRGB
    globalAlpha = render.strokeColor.alpha
    setLineDash(render.strokeDash ?: emptyArray())
    drawVerticalLine(x, y, height)
    globalAlpha = 1.0
}

private fun CanvasRenderingContext2D.drawVerticalLine(x: Double, y: Double, height: Double) {
    beginPath()
    moveTo(x, y)
    lineTo(x, y + height)
    closePath()
    stroke()
}

private fun CanvasRenderingContext2D.drawHorizontalLine(x: Double, y: Double, width: Double) {
    beginPath()
    moveTo(x, y)
    lineTo(x + width, y)
    closePath()
    stroke()
}