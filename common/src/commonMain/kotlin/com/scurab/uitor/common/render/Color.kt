package com.scurab.uitor.common.render

import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

@Suppress("MemberVisibilityCanBePrivate")
data class Color(val value: Int) {

    val alpha: Double = (value ushr 24) / 255.0
    val red: Int = (value ushr 16 and 0xFF)
    val green: Int = (value ushr 8 and 0xFF)
    val blue: Int = (value and 0xFF)

    private val alphaAs2Decimals: String
        get() = ((alpha * 100).roundToInt() / 100.0).toString()

    val htmlRGB = "#${red.hex}${green.hex}${blue.hex}"
    val htmlARGB = "#${(alpha * 255).roundToInt().hex}${red.hex}${green.hex}${blue.hex}"
    val htmlRGBA = "rgba($red, $green, $blue, $alphaAs2Decimals)"

    fun withAlpha(alpha: Double): Color {
        val v = value and 0x00FFFFFF//remove alpha
        val alpha = (min(1.0, max(0.0, alpha)) * 255.0).roundToInt()
        return Color(v or (alpha shl 24))
    }

    companion object {
        val Red = Color(0xFFFF0000.toInt())
        val Yellow = Color(0xFFFFFF00.toInt())
        val Gray20 = Color(0xFF333333.toInt())

        fun fromBytes(a: Byte, r: Byte, g: Byte, b: Byte): Color {
            val value = (a.toInt() shl 24) or (r.toInt() shl 16) or (g.toInt() shl 8) or (b.toInt() shl 0)
            return Color(value)
        }
    }
}

private val CSS_COLOR_REGEX = "rgba?\\((.*)\\)".toRegex()
fun String.toColor(): Color {
    var temp = substringAfter("#")
    val result = if (temp.startsWith("rgb")) {
        parseFromCss()
    } else {
        when {
            temp.startsWith("rgb") -> parseFromCss()
            temp.length == 3 -> temp = "F$temp".map { "$it$it" }.joinToString("")
            temp.length == 4 -> temp = temp.map { "$it$it" }.joinToString("")
            temp.length == 6 -> temp = "FF$temp"
            temp.length == 8 -> {
            }//ok nothing
        }
        return Color(temp.toLong(16).toInt())
    }
    return result ?: throw IllegalArgumentException("Invalid format of string color:'$this'")
}

private fun String.parseFromCss(): Color? {
    return CSS_COLOR_REGEX.find(this)?.groupValues
        ?.takeIf { it.size == 2 }
        ?.get(1)
        ?.split(",")
        ?.map { it.trim() }
        ?.takeIf { it.size in (3..4) }
        ?.let {
            val r = it[0].toInt()
            val g = it[1].toInt()
            val b = it[2].toInt()
            val a = if (it.size == 4) (it[3].toFloat() * 255).roundToInt() else 255
            Color(
                (a and 0xFF shl 24) or
                        (r and 0xFF shl 16) or
                        (g and 0xFF shl 8) or
                        (b and 0xFF shl 0)
            )
        }
}