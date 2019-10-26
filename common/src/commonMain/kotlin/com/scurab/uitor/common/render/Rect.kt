package com.scurab.uitor.common.render

data class Rect(val left: Int, val top: Int, val width: Int, val height: Int) {
    val right: Int = left + width
    val bottom: Int = top + height

    fun contains(x: Int, y: Int): Boolean {
        return left <= x && x <= right && top <= y && y <= bottom
    }

    fun scale(scaleX: Double, scaleY: Double = scaleX): Rect {
        return Rect(
            left.scaled(scaleX),
            top.scaled(scaleY),
            width.scaled(scaleX),
            height.scaled(scaleY)
        )
    }

    /**
     * Scale size of the rectangle, keeping the origin on same place
     */
    fun scaleSize(scaleX: Double, scaleY: Double = scaleX): Rect {
        return Rect(
            left,
            top,
            width.scaled(scaleX),
            height.scaled(scaleY)
        )
    }

    fun toIntArray() = intArrayOf(left, top, width, height)

    fun addRelative(render: Rect): Rect = Rect(
        left + render.left,
        top + render.top,
        render.width,
        render.height
    )
}