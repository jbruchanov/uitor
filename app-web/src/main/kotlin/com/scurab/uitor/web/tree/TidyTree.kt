package com.scurab.uitor.web.tree

import com.scurab.uitor.common.render.toColor
import com.scurab.uitor.common.util.dlog
import com.scurab.uitor.web.model.ViewNode
import d3.*
import org.w3c.dom.svg.SVGElement

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
        val (root, width, height, x0) = config.layout(data, config)

        val svg = d3.svg()
            //.viewBox(Rect(0, 0, width, (x1 - x0 + root.dx * 2).roundToInt()))
            //custom size (scrollable)
            .style("overflow-x: auto; overflow-y: auto")
            .width(width)
            .height(height)

        val g = svg.group()
            .transform(config.graphTranslation(config, root, x0))

        val link = g.group()
            .fill("none")
            .stroke("#dfdfdf".toColor())
            .strokeWidth(1.0)
            .selectAll("path")
            .data(root.links())
            .join("path")
            .attr("d", config.drawingPath())

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
                dlog(TAG) {
                    val item = n.item
                    "SelectedNode:${item.position} Level:${item.level} x:${n.x} y:${n.y}"
                }
                selectedNode?.circle?.setClass(CSS_CIRCLE)
                selectedNode = n.apply {
                    circle.setClass(CSS_CIRCLE_SELECTED)
                }
            }
            .transform { d -> config.translateNode(d) }
        node.append("circle")
            .classes(CSS_CIRCLE)
            .radius(config.circleRadius)

        node.append("text")
            .classes(CSS_NODE_TYPE)
            .onMouseOver { dlog(TAG) { it.item.type } }
            .onMouseLeave { }
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

}

internal fun SVGElement.setClass(name: String) {
    asDynamic().className.baseVal = name
}

class TreeConfig(
    //distance between nodes vertically
    val nodeOffsetX: Double,
    //distance between nodes horizontally
    val nodeOffsetY: Double,
    val circleRadius: Double,
    val showViewIds: Boolean,
    val viewGroupAnchorEnd: Boolean,
    private val delegate: LayoutRenderDelegate,
    val nodeTitleSelector: (Node<*>) -> String
) : LayoutRenderDelegate by delegate {
    companion object {
        fun defaultTidyTree(): TreeConfig {
            return TreeConfig(
                175.0, 30.0, 5.0,
                showViewIds = true,
                viewGroupAnchorEnd = false,
                delegate = HorizontalDelegate(4)
            ) { it.item.typeSimple }
        }

        fun shortTypesTidyTree(): TreeConfig {
            return TreeConfig(
                62.5,
                25.0,
                7.5,
                showViewIds = false,
                viewGroupAnchorEnd = false,
                delegate = HorizontalDelegate()
            ) {
                it.item.typeAbbr
            }
        }

        fun verticalSimpleTree(): TreeConfig {
            return TreeConfig(
                25.0,
                30.0,
                10.0,
                showViewIds = false,
                viewGroupAnchorEnd = false,
                delegate = VerticalDelegate()
            ) { "" }
        }
    }
}