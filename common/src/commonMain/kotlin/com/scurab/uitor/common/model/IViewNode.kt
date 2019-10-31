package com.scurab.uitor.common.model

import com.scurab.uitor.common.render.Rect

interface IViewNode {
    val nodes: List<IViewNode>?
    val idi: Int
    val ids: String?
    val level: Int
    val position: Int
    val data: Map<String, Any?>
    val owner: String?

    val rect: Rect
    fun findFrontVisibleView(x: Int, y: Int, ignore: Set<Int> = emptySet()): IViewNode?
}