package com.scurab.uitor.web.inspector

import com.scurab.uitor.common.util.npe
import kotlinx.html.canvas
import kotlinx.html.dom.create
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLCanvasElement
import util.px
import kotlin.browser.document
import kotlin.browser.window
import kotlin.random.Random

class CanvasTest {
    fun test() {
        val canvas: HTMLCanvasElement = document.create.canvas {
            width = 500.px
            height = (window.screen.height / 2).px
        } as HTMLCanvasElement
        (document.getElementById("root") ?: npe("Missing 'root' element")).append(canvas)
        FancyLines(canvas).run()
    }
}

class FancyLines(canvas: HTMLCanvasElement) {
    val context = canvas.getContext("2d") as CanvasRenderingContext2D
    val height = canvas.height.toDouble()
    val width = canvas.width.toDouble()
    fun nextX() = Random.nextDouble(width)
    fun nextY() = Random.nextDouble(height)
    var x = nextX()
    var y = nextY()
    var hue = 0

    fun line() {
        context.save();

        context.beginPath();

        context.lineWidth = Random.nextDouble(20.0)
        context.moveTo(x, y);

        x = nextX()
        y = nextY()

        context.bezierCurveTo(
            nextX(), nextY(),
            nextX(), nextY(), x, y
        )

        hue += Random.nextInt(10)

        context.strokeStyle = "hsl($hue, 50%, 50%)";

        context.shadowColor = "white";
        context.shadowBlur = 10.0;

        context.stroke();

        context.restore();
    }

    fun blank() {
        context.fillStyle = "rgba(255,255,1,0.1)";
        context.fillRect(0.0, 0.0, width, height);
    }

    fun run() {
        window.setInterval({ line() }, 40);
        window.setInterval({ blank() }, 100);
    }
}