package com.scurab.uitor.web.inspector

import com.scurab.uitor.web.coroutine.RememberLastItemChannel
import com.scurab.uitor.common.util.isMatchingIndexes
import com.scurab.uitor.common.util.matchingIndexes
import com.scurab.uitor.web.ui.HtmlView
import com.scurab.uitor.web.util.highlightAt
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.html.*
import kotlinx.html.dom.create
import kotlinx.html.js.onKeyUpFunction
import org.w3c.dom.Element
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLInputElement
import kotlin.browser.document
import kotlin.dom.clear
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.html.stream.createHTML

private val CSS_PROPERTIES_TABLE = "properties"
private val CSS_PROPERTIES_HEADER = "properties-header"
private val CSS_PROPERTIES_HEADER_TITLE = "properties-header-title"
private val CSS_PROPERTIES_COLOR = "properties-color"
private val CSS_PROPERTIES_EVEN = "properties-even"
private val CSS_PROPERTIES_ODD = "properties-odd"

class PropertiesView(
    private val rootElement: Element,
    private val inspectorViewModel: InspectorViewModel
) : HtmlView {

    private val TAG = "PropertiesView"
    override lateinit var element: HTMLElement
        private set

    private val contentRoot: HTMLElement
    private val tableRootElement: HTMLElement
    private val filterChannel = RememberLastItemChannel<String>()

    init {
        contentRoot = document.create.div {
            div {
                table(classes = "properties-filter") {
                    tr {
                        td {
                            textInput(classes = "properties-filter") {
                                id = "properties-filter"
                                onKeyUpFunction = {
                                    val filterValue = (it.currentTarget as HTMLInputElement).value.toLowerCase()
                                    filterChannel.offer(filterValue)
                                }
                            }
                        }
                    }
                }
            }
            div { id = "properties-table" }
        }.apply {
            rootElement.append(this)
        }

        tableRootElement = document.getElementById("properties-table") as HTMLElement
        inspectorViewModel.selectedNode.observe {
            rebuildHtml(filterChannel.lastItem ?: "")
        }

        GlobalScope.launch {

            filterChannel.consumeAsFlow().debounce(100).collect {
                rebuildHtml(it)
            }
        }
    }

    override fun attach(): HtmlView {
        return this
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
                            span { text("Value") }
                        }
                    }
                }
                var i = 0
                root.data.forEach { (keyRaw, v) ->
                    val key = keyRaw.substringBefore(":")
                    val value = v?.toString()
                    val link = keyRaw.endsWith(":")

                    val matchingIndexes = key.matchingIndexes(filter)
                    val matchingFilter = filter.isEmpty()
                            || matchingIndexes.isNotEmpty() || value?.contains(filter) == true

                    if (!matchingFilter) {
                        return@forEach
                    }

                    tr(classes = if (i % 2 == 0) CSS_PROPERTIES_EVEN else CSS_PROPERTIES_ODD) {
                        td {
                            span(classes = CSS_PROPERTIES_COLOR) {
                                text(" ")
                            }
                        }
                        td {
                            if (link) {
                                val url =
                                    "#ViewProperty?screenIndex=${inspectorViewModel.screenIndex}&position=${root.position}&property=$key"
                                a(href = url, target = "_blank") {
                                    span { unsafe { raw(key.highlightAt(matchingIndexes, "<b>", "</b>")) } }
                                }
                            } else {
                                span { unsafe { raw(key.highlightAt(matchingIndexes, "<b>", "</b>")) } }
                            }
                        }
                        td { span { text(v?.toString() ?: "") } }
                    }
                    i++
                }
            }
        }.apply {
            tableRootElement.append(this)
        }
    }
}


