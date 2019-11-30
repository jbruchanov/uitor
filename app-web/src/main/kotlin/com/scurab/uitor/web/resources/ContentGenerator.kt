package com.scurab.uitor.web.resources

import com.scurab.uitor.common.render.toColor
import com.scurab.uitor.web.model.IResourceItem
import com.scurab.uitor.web.model.ResourceItem
import kotlinx.html.DIV
import kotlinx.html.div
import kotlinx.html.dom.create
import kotlinx.html.img
import kotlinx.html.js.div
import kotlinx.html.pre
import kotlinx.html.style
import org.w3c.dom.HTMLElement
import kotlin.browser.document

private const val CSS_RESOURCES = "resources-item-content"
private const val CSS_PRETTY_PRINT = "prettyprint"
private const val CSS_PRETTY_PRINT_NONE = "prettyprint prettyprinted"

class ResourcesContentGenerator {

    fun buildFullDescriptionContent(item: ResourceItem): HTMLElement {
        return document.create.div(classes = CSS_RESOURCES) {
            div { text("ID:${item.id}") }
            div { text("Name:${item.name}") }
            div { text("Context:${item.source}") }
            div { text("_______________________________________________________") }
            div { text("Data:") }
            buildDataContent(item)
        }
    }

    fun buildContent(item: IResourceItem): HTMLElement {
        return document.create.div(classes = CSS_RESOURCES) {
            buildDataContent(item)
        }
    }

    private fun DIV.buildDataContent(item: IResourceItem) {
        when (item.dataType) {
            null -> {/*nothing*/}
            "xml" -> pre(classes = CSS_PRETTY_PRINT) { text(item.dataString()) }
            "string", "boolean", "number", "int" -> pre(classes = CSS_PRETTY_PRINT_NONE) { text(item.dataString()) }
            "string[]", "int[]" -> {
                pre(classes = CSS_PRETTY_PRINT_NONE) {
                    item.dataArrayPrimitives().forEach {
                        div { text(it.toString()) }
                    }
                }
            }
            "array" -> item.dataArrayResourcesItems().forEach { buildDataContent(it) }
            "color" -> color(item)
            "base64_png" -> image(item)
            else -> div { text(item.dataString()) }
        }
    }

    private fun DIV.color(item: IResourceItem) {
        val c = item.dataString()
        val color = c.toColor()
        div { text(c) }
        div {
            style = "height:50px; width:200px; background:url(transparent.png);"
            div {
                style = "height:50px; width:200px; background:${color.htmlRGBA};"
            }
        }
    }

    private fun DIV.image(item: IResourceItem) {
        div {
            div { text(item.context ?: "") }
            img(classes = "transparent resources-item-property-preview") {
                src = "data:image/png;base64,${item.dataString()}"
            }
        }

    }
}
