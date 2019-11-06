package com.scurab.uitor.web.tree

import com.scurab.uitor.web.model.ViewNode
import js.d3.Node
import org.w3c.dom.get
import org.w3c.dom.svg.SVGCircleElement
import kotlin.browser.document

internal val Node<*>.item get() = data as? ViewNode ?: throw NullPointerException("Field: data is not ViewNode")
internal val Node<*>.groupId get() = "ViewNode:${item.position}"
internal val Node<*>.circle: SVGCircleElement
    get() {
        return document.getElementById(groupId)?.getElementsByTagName("circle")?.get(0) as? SVGCircleElement
            ?: throw IllegalStateException("Unable to find 'circle' in node:'$item'")
    }

internal fun Node<*>.textAnchor(config: TreeConfig) =
    if (config.viewGroupAnchorEnd && children?.isNotEmpty() == true) "end" else "start"

internal fun Node<*>.textAnchorX(config: TreeConfig) =
    config.circleRadius / 2 + if (config.viewGroupAnchorEnd && children?.isNotEmpty() == true) -10.0 else 10.0