package com.scurab.uitor.common.render

import kotlin.math.roundToInt

@Suppress("MemberVisibilityCanBePrivate")
data class Color(val value: Int) {
    val alpha: Double = (value ushr 24) / 255.0
    val red: Int = (value ushr 16 and 0xFF)
    val green: Int = (value ushr 8 and 0xFF)
    val blue: Int = (value and 0xFF)

    val htmlRGB = "#${red.hex}${green.hex}${blue.hex}"
    val htmlARGB = "#${(alpha * 255).roundToInt().hex}${red.hex}${green.hex}${blue.hex}"
    val htmlRGBA = "rgba($red, $green, $blue, $alpha)"
}

fun String.toColor(): Color {
    var temp = substringAfter("#")
    when {
        temp.length == 3 -> temp = "F$temp".map { "$it$it" }.joinToString("")
        temp.length == 4 -> temp = temp.map { "$it$it" }.joinToString("")
        temp.length == 6 -> temp = "FF$temp"
        temp.length == 8 -> {}//ok nothing
        else -> throw IllegalArgumentException("Invalid format of string color:'$this'")
    }
    return Color(temp.toLong(16).toInt())
}