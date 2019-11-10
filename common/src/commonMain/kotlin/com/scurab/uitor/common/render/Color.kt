package com.scurab.uitor.common.render

import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

@Suppress("MemberVisibilityCanBePrivate")
data class Color(val value: Int) {

    constructor(alpha: Int, red: Int, green: Int, blue: Int) : this(
        (alpha and 0xFF shl 24) or
                (red and 0xFF shl 16) or
                (green and 0xFF shl 8) or
                (blue and 0xFF shl 0)
    )

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

    fun toHSV() : FloatArray {
        val rf = red / 255f; val gf = green / 255f; val bf = blue / 255f

        val max = max(rf, max(gf, bf))
        val min = min(rf, min(gf, bf))
        val deltaMaxMin = max - min

        var h: Float; var s: Float; var l = (max + min) / 2f

        if (max == min) {
            // Monochromatic
            h = 0f
            s = 0f
        } else {
            h = when (max) {
                rf -> ((gf - bf) / deltaMaxMin) % 6f
                gf -> ((bf - rf) / deltaMaxMin) + 2f
                else -> ((rf - gf) / deltaMaxMin) + 4f
            }
            s = deltaMaxMin / (1f - abs(2f * l - 1f))
        }

        h = (h * 60f) % 360f;
        if (h < 0) {
            h += 360f;
        }

        return floatArrayOf(
            constrain(h, 0f, 360f),
            constrain(s, 0f, 1f),
            constrain(l, 0f, 1f)
        )
    }

    fun halfLightness(): Color {
        val toHSV = toHSV()
        toHSV[2] /= 2f
        return fromHSL(toHSV)
    }

    companion object {
        val Red = Color(0xFFFF0000.toInt())
        val Yellow = Color(0xFFFFFF00.toInt())
        val Gray20 = Color(0xFF333333.toInt())

        fun fromBytes(a: Byte, r: Byte, g: Byte, b: Byte): Color {
            val value = (a.toInt() shl 24) or (r.toInt() shl 16) or (g.toInt() shl 8) or (b.toInt() shl 0)
            return Color(value)
        }

        fun fromHSL(hsl: FloatArray) : Color {
            val h = hsl[0]; val s = hsl[1]; val l = hsl[2]
            val c = (1f - abs(2 * l - 1f)) * s;
            val m = l - 0.5f * c;
            val x = c * (1f - abs((h / 60f % 2f) - 1f));
            val hueSegment = h.toInt() / 60
            var r = 0f; var g = 0f; var b = 0f

            when (hueSegment) {
                0 -> { r = (255 * (c + m)); g = (255 * (x + m)); b = (255 * m) }
                1 -> { r = (255 * (x + m)); g = (255 * (c + m)); b = (255 * m) }
                2 ->  { r = (255 * m); g = (255 * (c + m)); b = (255 * (x + m)) }
                3 -> { r = (255 * m); g = (255 * (x + m)); b = (255 * (c + m)) }
                4 -> { r = (255 * (x + m)); g = (255 * m); b = (255 * (c + m)) }
                5, 6 -> { r = (255 * (c + m)); g = (255 * m); b = (255 * (x + m)) }
            }

            val ir = constrain(r.roundToInt(), 0, 255)
            val ig = constrain(g.roundToInt(), 0, 255)
            val ib = constrain(b.roundToInt(), 0, 255)

            return Color(255, ir, ig, ib);
        }

        private fun constrain(amount: Float, low: Float, high: Float): Float {
            return if (amount < low) low else if (amount > high) high else amount
        }

        private fun constrain(amount: Int, low: Int, high: Int): Int {
            return if (amount < low) low else if (amount > high) high else amount
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