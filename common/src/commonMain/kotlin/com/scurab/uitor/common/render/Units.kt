@file:Suppress("NAME_SHADOWING")

package com.scurab.uitor.common.render

import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
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


fun Double.relativeToScale(scale: Double) = this / scale

val Number.r2 get() = roundWithDecimalPlaces(2)
fun Number.roundWithDecimalPlaces(decimals: Int = 2): String {
    val decimals = max(1, min(decimals, 8))
    val d = 10.0.pow(decimals)
    return ((this.toDouble() * d).roundToInt() / d).toString()
}