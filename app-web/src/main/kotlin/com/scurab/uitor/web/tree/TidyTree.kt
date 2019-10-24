package com.scurab.uitor.web.tree

import com.scurab.uitor.common.render.toColor
import com.scurab.uitor.common.util.dlog
import com.scurab.uitor.web.model.ViewNode
import d3.*
import org.w3c.dom.get
import org.w3c.dom.svg.SVGCircleElement
import org.w3c.dom.svg.SVGElement
import kotlin.browser.document
import kotlin.math.max

/**
 * Inspired from https://observablehq.com/@d3/tidy-tree
 */
private const val CSS_NODE = "ui-tree-node"
private const val CSS_CIRCLE = "ui-tree-circle"
private const val CSS_CIRCLE_SELECTED = "ui-tree-circle-selected"
private const val CSS_NODE_TYPE = "ui-tree-node-type"
private const val CSS_NODE_ID = "ui-tree-node-id"

class TidyTree {

    private val TAG = "TidyTree"

    fun generateSvg(data: ViewNode, config: TreeConfig = TreeConfig.shortTypesTidyTree()): SVGElement {
        val (root, width, height, x0) = layout(data, config)

        val svg = d3.svg()
            //.viewBox(Rect(0, 0, width, (x1 - x0 + root.dx * 2).roundToInt()))
            //custom size (scrollable)
            .style("overflow-x: auto; overflow-y: auto")
            .width(width)
            .height(height)

        val g = svg.group()
            .transform(
                translate(
                    if (config.viewGroupAnchorEnd) config.nodeOffsetX else config.nodeOffsetY / 2,
                    root.dx - x0
                )
            )

        val link = g.group()
            .fill("none")
            .stroke("#dfdfdf".toColor())
            .strokeWidth(1.0)
            .selectAll("path")
            .data(root.links())
            .join("path")
            .attr("d", d3.linkHorizontal()
                .x { it.y }
                .y { it.x })

        var selectedNode: Node<*>? = null
        val node = g.group()
            .classes(CSS_NODE)
            .strokeLineJoin("round")
            .strokeWidth(3.0)
            .selectAll("g")
            .data(root.descendants())
            .join("g")
            .attr("id") { v: Node<*> -> v.groupId }
            .onClick { n ->
                selectedNode?.circle?.setClass(CSS_CIRCLE)
                selectedNode = n.apply {
                    circle.setClass(CSS_CIRCLE_SELECTED)
                }
            }
            .transform { d ->
                translate(d.y, d.x)
            }
        node.append("circle")
            .classes(CSS_CIRCLE)
            .radius(config.circleRadius)

        node.append("text")
            .classes(CSS_NODE_TYPE)
            .onMouseOver { dlog(TAG) { it.item.type } }
            .onMouseLeave {  }
            .dy { d -> if (!config.showViewIds || d.item.ids == null) "0.31em" else "-.2em" }
            .x { d: Node<*> -> d.textAnchorX(config) }
            .textAnchor { d: Node<*> -> d.textAnchor(config) }
            .text { d: Node<*> -> config.nodeTitleSelector(d) }
            .clone(true).lower()
            .stroke("#FFF".toColor())

        if (config.showViewIds) {
            node.append("text")
                .classes(CSS_NODE_ID)
                .dy { d -> if (d.item.ids != null) "1.1em" else "0em" }
                .x { d: Node<*> -> d.textAnchorX(config) }
                .textAnchor { d: Node<*> -> d.textAnchor(config) }
                .text { d: Node<*> ->
                    d.item.ids ?: ""
                }
                .clone(true).lower()
                .stroke("#FFF".toColor())
        }
        return svg.node()
    }

    private fun layout(root: ViewNode, config: TreeConfig): LayoutResult {
        var r = d3.hierarchy(root) { it.nodes.toTypedArray() }
        r.dx = max(2 * config.circleRadius, config.nodeOffsetY)
        r.dy = config.circleRadius + config.nodeOffsetX/*width.toDouble() / (root.height + 1)*/
        val tree = d3.tree()
        val treeBuilder = tree.nodeSize(doubleArrayOf(r.dx, r.dy))
        r = treeBuilder(r)
        var x0 = Double.POSITIVE_INFINITY
        var x1 = Double.NEGATIVE_INFINITY
        r.each { d ->
            if (d.x > x1) x1 = d.x;
            if (d.x < x0) x0 = d.x;
        }
        //.viewBox(Rect(0, 0, width, (x1 - x0 + root.dx * 2).roundToInt()))
        return LayoutResult(
            r,
            (r.height + 1 + (if (config.viewGroupAnchorEnd) 1 else 0)) * r.dy,//+2 => 1 column for name + last column for name
            (x1 - x0 + r.dx * 2),
            x0
        )
    }
}

data class TreeConfig(
    //distance between nodes vertically
    val nodeOffsetX: Double,
    //distance between nodes horizontally
    val nodeOffsetY: Double,
    val circleRadius: Double,
    val showViewIds: Boolean,
    val viewGroupAnchorEnd: Boolean,
    val nodeTitleSelector: (Node<*>) -> String
) {
    companion object {
        fun defaultTidyTree(): TreeConfig {
            return TreeConfig(175.0, 35.0, 5.0, showViewIds = true, viewGroupAnchorEnd = false) {
                it.item.typeSimple
            }
        }

        fun shortTypesTidyTree(): TreeConfig {
            return TreeConfig(62.5, 25.0, 7.5, showViewIds = false, viewGroupAnchorEnd = false) {
                it.item.typeAbbr
            }
        }
    }
}

private val Node<*>.item get() = data as? ViewNode ?: throw NullPointerException("Field: data is not ViewNode")
private val Node<*>.groupId get() = "ViewNode:${item.position}"
private val Node<*>.circle: SVGCircleElement
    get() {
        return document.getElementById(groupId)?.getElementsByTagName("circle")?.get(0) as? SVGCircleElement
            ?: throw IllegalStateException("Unable to find 'circle' in node:'$item'")
    }

private fun Node<*>.textAnchor(config: TreeConfig) =
    if (config.viewGroupAnchorEnd && children?.isNotEmpty() == true) "end" else "start"

private fun Node<*>.textAnchorX(config: TreeConfig) =
    config.circleRadius / 2 + if (config.viewGroupAnchorEnd && children?.isNotEmpty() == true) -10.0 else 10.0

private fun SVGElement.setClass(name: String) {
    asDynamic().className.baseVal = name
}

private data class LayoutResult(
    val node: Node<*>,
    val width: Double,
    val height: Double,
    val x0: Double
)