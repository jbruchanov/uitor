package com.scurab.uitor.common.render

import kotlin.math.roundToInt

val Int.px get() = "${this}px"
val Int.d get() = this.toDouble()
fun Int.scaled(scale: Double) = (this * scale).roundToInt()
val Int.hex
    get() =
        toString(16).let {
            if (it.length == 1) {
                "0$it"
            } else it
        }.toUpperCase()


fun Double.relativeToScale(scale:Double) = this / scale