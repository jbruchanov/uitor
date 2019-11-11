package com.scurab.uitor.web.tree

import com.scurab.uitor.common.util.dlog
import com.scurab.uitor.common.util.ellipsizeMid
import com.scurab.uitor.web.model.ClientConfig
import com.scurab.uitor.web.model.ViewNode
import js.d3.Node
import js.d3.classes
import js.d3.dy
import js.d3.group
import js.d3.height
import js.d3.onClick
import js.d3.onMouseLeave
import js.d3.onMouseOver
import js.d3.radius
import js.d3.stroke
import js.d3.strokeLineJoin
import js.d3.strokeWidth
import js.d3.style
import js.d3.textAnchor
import js.d3.transform
import js.d3.width
import js.d3.x
import org.w3c.dom.svg.SVGCircleElement
import org.w3c.dom.svg.SVGElement

/**
 * Inspired from https://observablehq.com/@d3/tidy-tree
 * Depends on
 * <script src="https://d3js.org/d3.v5.min.js"></script>
 */
private const val CSS_NODE = "ui-tree-node"
private const val CSS_CIRCLE = "ui-tree-circle"
private const val CSS_CIRCLE_SELECTED = "ui-tree-circle-selected"
private const val CSS_NODE_TYPE = "ui-tree-node-type"
private const val CSS_NODE_LINKS = "ui-tree-node-links"
private const val CSS_NODE_ID = "ui-tree-node-id"

class TidyTree {

    private val TAG = "TidyTree"

    fun generateSvg(data: ViewNode,
                    preSelectedNode: ViewNode? = null,
                    config: TreeConfig = TreeConfig.shortTypesTidyTree,
                    clientConfig: ClientConfig,
                    nodeClickListener: (ViewNode) -> Unit
    ): SVGElement {
        val (root, width, height, x0) = config.layout(data, config)

        val svg = js.d3.svg()
            //.viewBox(Rect(0, 0, width, (x1 - x0 + root.dx * 2).roundToInt()))
            //custom size (scrollable)
            .style("overflow-x: auto; overflow-y: auto")
            .width(width)
            .height(height)

        val g = svg.group()
            .transform(config.graphTranslation(config, root, x0))

        val link = g.group()
            .classes(CSS_NODE_LINKS)
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
                nodeClickListener(n.item)
            }
            .transform { d -> config.translateNode(d) }

        node.append("circle")
            .classes(CSS_CIRCLE)
            .radius(config.circleRadius)
            .style { d: Node<*> -> d.item.highlightedStyle(clientConfig) }
            .attr("_id") { v: Node<*>, i: Int, items: Array<SVGCircleElement> ->
                //just workaround how to iterate through nodes/circles and set the selected one
                if (v.item == preSelectedNode) {
                    selectedNode = v
                    items[i].setClass(CSS_CIRCLE_SELECTED)
                }
                v.groupId
            }

        node.append("text")
            .classes(CSS_NODE_TYPE)
            .onMouseOver { dlog(TAG) { it.item.type } }
            .onMouseLeave { }
            .dy { d -> if (!config.showViewIds || d.item.ids == null) "0.31em" else "-.2em" }
            .x { d: Node<*> -> d.textAnchorX(config) }
            .textAnchor { d: Node<*> -> d.textAnchor(config) }
            .text { d: Node<*> -> config.nodeTitleSelector(d) }
            .clone(true).lower()

        node.append("title")
            .text { "${it.item.type}\n${it.item.ids ?: ""}" }

        if (config.showViewIds) {
            node.append("text")
                .classes(CSS_NODE_ID)
                .dy { d -> if (d.item.ids != null) "1.1em" else "0em" }
                .x { d: Node<*> -> d.textAnchorX(config) }
                .textAnchor { d: Node<*> -> d.textAnchor(config) }
                .text { d: Node<*> -> d.item.ids?.idEllipsizedMid(d.depth) ?: "" }
                .clone(true).lower()
        }
        return svg.node()
    }
}

private fun ViewNode.highlightedStyle(clientConfig: ClientConfig) : String {
    return clientConfig.typeHighlights[type]?.let {
        "stroke:${it.htmlRGBA}"
    } ?: ""
}

private fun String.idEllipsizedMid(nodeDepth: Int): String {
    return if(nodeDepth < TreeConfig.DENSE_COLS && this.length > 11) {
        ellipsizeMid(14)
    } else {
        ellipsizeMid(24)
    }
}

private fun String.typeEllipsizedMid(nodeDepth: Int): String {
    return if(nodeDepth < TreeConfig.DENSE_COLS && length > 11) {
        ellipsizeMid(10)
    } else {
        ellipsizeMid(21)
    }
}

internal fun SVGElement.setClass(name: String) {
    asDynamic().className.baseVal = name
}

class TreeConfig(
    val id: String,
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
        internal const val DENSE_COLS = 4
        val defaultTidyTree: TreeConfig = TreeConfig(
            "default",
            175.0, 30.0, 5.0,
            showViewIds = true,
            viewGroupAnchorEnd = false,
            delegate = HorizontalDelegate(DENSE_COLS)
        ) { it.item.typeSimple.typeEllipsizedMid(it.depth) }

        val shortTypesTidyTree = TreeConfig(
            "shortTypes",
            62.5,
            25.0,
            7.5,
            showViewIds = false,
            viewGroupAnchorEnd = false,
            delegate = HorizontalDelegate()
        ) { it.item.typeAbbr }

        val verticalSimpleTree =
            TreeConfig(
                "simple",
                25.0,
                30.0,
                10.0,
                showViewIds = false,
                viewGroupAnchorEnd = false,
                delegate = VerticalDelegate()
            ) { "" }
    }
}
