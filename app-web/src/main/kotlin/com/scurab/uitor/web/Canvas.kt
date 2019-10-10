package com.scurab.uitor.web

import org.w3c.dom.CanvasRenderingContext2D

fun CanvasRenderingContext2D.clear() {
    clearRect(0.0, 0.0, canvas.width.toDouble(), canvas.height.toDouble());
}

fun CanvasRenderingContext2D.drawRectangle(x: Int, y: Int, w: Int, h: Int, stroke: String, fill: String) {
    drawRectangle(x.toDouble(), y.toDouble(), w.toDouble(), h.toDouble(), stroke, fill)
}

fun CanvasRenderingContext2D.drawRectangle(x: Double, y: Double, w: Double, h: Double, stroke: String, fill: String) {
    lineWidth = 1.0
    strokeStyle = stroke
    drawHorizontalLine(x, y, w)
    drawHorizontalLine(x, y + h, w)
    drawVerticalLine(x, y, h)
    drawVerticalLine(x + w, y, h)

    globalAlpha = 0.05
    fillStyle = fill
    fillRect(x, y, w, h)
    globalAlpha = 1.0
}

fun CanvasRenderingContext2D.drawVerticalLine(x: Double, y: Double, height: Double) {
    beginPath()
    moveTo(x, y)
    lineTo(x, y + height)
    closePath()
    stroke()
}

fun CanvasRenderingContext2D.drawHorizontalLine(x: Double, y: Double, width: Double) {
    beginPath()
    moveTo(x, y)
    lineTo(x + width, y)
    closePath()
    stroke()
}