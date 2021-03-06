package com.scurab.uitor.web.tree

import com.scurab.uitor.web.model.ViewNode
import js.d3.Link
import js.d3.Node
import js.d3.translate
import kotlin.math.max

//just safety gap if the tree is too small in some size
const val SAFETY_GAP = 20
interface LayoutRenderDelegate {
    fun translateNode(d: Node<*>): String
    fun drawingPath(): Link
    fun graphTranslation(config: TreeConfig, root: Node<*>, x0: Double): String
    fun layout(root: ViewNode, config: TreeConfig): LayoutResult
    fun nodeDistances(config: TreeConfig): Pair<Double, Double>

    fun LayoutRenderDelegate.buildTree(root: ViewNode, config: TreeConfig): Node<*> {
        val (dx, dy) = nodeDistances(config)
        val r = js.d3.hierarchy(root) { it.nodes.toTypedArray() }
        r.dx = dx
        r.dy = dy
        val tree = js.d3.tree()
        val treeBuilder = tree.nodeSize(doubleArrayOf(r.dx, r.dy))
        return treeBuilder(r)
    }
}

data class LayoutResult(
    val node: Node<*>,
    val width: Double,
    val height: Double,
    val x0: Double
)

/**
 * Layout delegate to have a horizontally-growing tree
 */
class HorizontalDelegate(private val denseFirstNColumns: Int = 0) : LayoutRenderDelegate {
    override fun translateNode(d: Node<*>): String = translate(d.y, d.x)
    override fun drawingPath(): Link = js.d3.linkHorizontal().x { it.y }.y { it.x }
    override fun graphTranslation(config: TreeConfig, root: Node<*>, x0: Double): String {
        return translate(
            if (config.viewGroupAnchorEnd) config.nodeOffsetX else config.nodeOffsetY / 2,
            root.dx - x0
        )
    }
    override fun nodeDistances(config: TreeConfig): Pair<Double, Double> {
        return Pair(
            max(2 * config.circleRadius, config.nodeOffsetY),
            config.circleRadius + config.nodeOffsetX/*width.toDouble() / (root.height + 1)*/
        )
    }

    override fun layout(root: ViewNode, config: TreeConfig): LayoutResult {
        val r = buildTree(root, config)
        var x0 = Double.POSITIVE_INFINITY
        var x1 = Double.NEGATIVE_INFINITY
        r.each { d ->
            val column = d.item.level
            //move first 4 columns more close to each other, it's android default view hierarchy, not so important
            val diff = when {
                column <= denseFirstNColumns -> column * 65
                else -> (denseFirstNColumns * 65)
            }
            d.y -= diff
            if (d.x > x1) x1 = d.x
            if (d.x < x0) x0 = d.x
        }
        //.viewBox(Rect(0, 0, width, (x1 - x0 + root.dx * 2).roundToInt()))
        return LayoutResult(
            r,
            SAFETY_GAP + ((r.height + 1 + (if (config.viewGroupAnchorEnd) 1 else 0)) * r.dy) - (denseFirstNColumns * 65),
            (x1 - x0 + r.dx * 2),
            x0
        )
    }
}

/**
 * Layout delegate to have a vertically-growing tree
 */
class VerticalDelegate : LayoutRenderDelegate {
    override fun translateNode(d: Node<*>): String = translate(d.x, d.y)
    override fun drawingPath(): Link = js.d3.linkVertical().x { it.x }.y { it.y }
    override fun graphTranslation(config: TreeConfig, root: Node<*>, x0: Double): String {
        return translate(
            root.dx - x0,
            if (config.viewGroupAnchorEnd) config.nodeOffsetX else config.nodeOffsetY / 2
        )
    }
    override fun nodeDistances(config: TreeConfig): Pair<Double, Double> {
        return Pair(
            max(2 * config.circleRadius, config.nodeOffsetX),
            config.circleRadius + config.nodeOffsetY
        )
    }

    override fun layout(root: ViewNode, config: TreeConfig): LayoutResult {
        val r = buildTree(root, config)
        var x0 = Double.POSITIVE_INFINITY
        var x1 = Double.NEGATIVE_INFINITY
        r.each { d ->
            if (d.x > x1) x1 = d.x
            if (d.x < x0) x0 = d.x
        }
        return LayoutResult(
            r,
            (x1 - x0 + r.dx * 2),
            (r.height + 1 + (if (config.viewGroupAnchorEnd) 1 else 0)) * r.dy,
            x0
        )
    }
}