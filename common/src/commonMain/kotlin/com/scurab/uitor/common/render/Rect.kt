package com.scurab.uitor.common.render

data class Rect(val left: Int, val top: Int, val width: Int, val height: Int) {
    val right: Int = left + width
    val bottom: Int = top + height

    fun contains(x: Int, y: Int): Boolean {
        return left <= x && x <= right && top <= y && y <= bottom
    }

    fun scale(scale: Double): Rect {
        return Rect(
            left.scaled(scale),
            top.scaled(scale),
            width.scaled(scale),
            height.scaled(scale)
        )
    }

    fun toIntArray() = intArrayOf(left, top, width, height)
}