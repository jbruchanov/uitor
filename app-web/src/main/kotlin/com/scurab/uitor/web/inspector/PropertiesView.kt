@file:Suppress("EXPERIMENTAL_API_USAGE")

package com.scurab.uitor.web.inspector

import com.scurab.uitor.common.render.Color
import com.scurab.uitor.common.util.highlightAt
import com.scurab.uitor.common.util.matchingIndexes
import com.scurab.uitor.web.ui.HtmlView
import com.scurab.uitor.web.util.requireElementById
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import kotlinx.html.*
import kotlinx.html.dom.create
import kotlinx.html.js.onKeyUpFunction
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLInputElement
import kotlin.browser.document
import kotlin.dom.clear

private const val CSS_PROPERTIES_TABLE = "properties"
private const val CSS_PROPERTIES_HEADER = "properties-header"
private const val CSS_PROPERTIES_HEADER_TITLE = "properties-header-title"
private const val CSS_PROPERTIES_COLOR = "properties-color"
private const val CSS_PROPERTIES_EVEN = "properties-even"
private const val CSS_PROPERTIES_ODD = "properties-odd"
private const val HTML_BOLD_START = "<b>"
private const val HTML_BOLD_END = "</b>"


class PropertiesView(
    private val inspectorViewModel: InspectorViewModel
) : HtmlView() {

    private val TAG = "PropertiesView"
    override lateinit var element: HTMLElement private set
    private lateinit var tableRootElement: HTMLElement
    private val filterChannel = ConflatedBroadcastChannel("")
    private val propertyHighlights = inspectorViewModel.clientConfig.propertyHighlights

    override fun buildContent() {
        element = document.create.div {
            div {
                table(classes = "properties-filter") {
                    tr {
                        td {
                            textInput(classes = "properties-filter") {
                                id = "properties-filter"
                                onKeyUpFunction = {
                                    val filterValue = (it.currentTarget as HTMLInputElement).value.trim()
                                    filterChannel.offer(filterValue)
                                }
                            }
                        }
                    }
                }
            }
            div { id = "properties-table" }
        }

        tableRootElement = element.requireElementById("properties-table")
        inspectorViewModel.selectedNode.observe {
            rebuildHtml(filterChannel.value ?: "")
        }

        GlobalScope.launch {
            filterChannel.asFlow().debounce(100).collect {
                rebuildHtml(it)
            }
        }
    }

    private fun rebuildHtml(filter: String = "") {
        tableRootElement.clear()
        val root = inspectorViewModel.selectedNode.item ?: return
        document.create.div {
            table(classes = CSS_PROPERTIES_TABLE) {
                thead {
                    tr {
                        td(classes = CSS_PROPERTIES_HEADER)
                        td(classes = CSS_PROPERTIES_HEADER_TITLE) {
                            span { text("Property") }
                        }
                        td(classes = CSS_PROPERTIES_HEADER_TITLE) {
                            attributes["style"] = "width:100%"
                            span { text("Value") }
                        }
                    }
                }
                root.dataSortedKeys.forEachIndexed { i, keyRaw ->
                    val key = keyRaw.substringBefore(":")
                    val value = root.data[keyRaw]?.toString()
                    val link = keyRaw.endsWith(":")

                    val matchingIndexes = key.matchingIndexes(filter)
                    val matchingFilter = filter.isEmpty()
                            || matchingIndexes.isNotEmpty() || value?.contains(filter, true) == true

                    if (!matchingFilter) {
                        return@forEachIndexed
                    }

                    tr(classes = if (i % 2 == 0) CSS_PROPERTIES_EVEN else CSS_PROPERTIES_ODD) {
                        td {
                            key.toPropertyHighlightColor()?.let {
                                span(classes = CSS_PROPERTIES_COLOR) {
                                    attributes["style"] = "background-color:${it.htmlRGBA}"
                                }
                            }
                        }
                        td {
                            if (link) {
                                val url =
                                    "#ViewProperty?screenIndex=${inspectorViewModel.screenIndex}&position=${root.position}&property=$key"
                                a(href = url, target = "_blank") {
                                    span { unsafe { raw(key.highlightAt(matchingIndexes, HTML_BOLD_START, HTML_BOLD_END)) } }
                                }
                            } else {
                                span { unsafe { raw(key.highlightAt(matchingIndexes, HTML_BOLD_START, HTML_BOLD_END)) } }
                            }
                        }
                        td {
                            span {
                                unsafe { raw(value?.highlightAt(filter, HTML_BOLD_START, HTML_BOLD_END) ?: "") }
                            }
                        }
                    }
                }
            }
        }.apply {
            tableRootElement.append(this)
        }
    }

    private fun String.toPropertyHighlightColor(): Color? {
        val v = this.toLowerCase()
        propertyHighlights.forEach { (r, c) ->
            if (r.matches(v)) {
                return c
            }
        }
        return null
    }
}


