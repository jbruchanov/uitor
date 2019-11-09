package com.scurab.uitor.web.screen

import com.scurab.uitor.common.util.ref
import com.scurab.uitor.web.common.Page
import com.scurab.uitor.web.model.PageViewModel
import com.scurab.uitor.web.model.ScreenNode
import com.scurab.uitor.web.ui.launchWithProgressBar
import com.scurab.uitor.web.util.lazyLifecycled
import com.scurab.uitor.web.util.requireElementById
import kotlinx.html.div
import kotlinx.html.id
import kotlinx.html.js.div
import kotlinx.html.span
import kotlinx.html.style
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement
import kotlin.dom.clear

private const val ID_CONTAINER = "screen-components-container"
private const val CSS_TYPE_NAME = "screen-components-type-name"
private const val CSS_PACKAGE_NAME = "screen-components-package-name"
private const val CSS_ROW_ODD = "screen-components-row-odd"
private const val CSS_ROW_EVEN = "screen-components-row-even"
private const val CSS_REF = "screen-components-ref"

class ScreenComponentsPage(private val pageViewModel: PageViewModel) : Page() {

    override fun stateDescription(): String? = null
    override var element: HTMLElement? = null; private set
    private val container by lazyLifecycled { element.ref.requireElementById<HTMLDivElement>(ID_CONTAINER) }

    override fun buildContent() {
        element = document.create.div {
            id = ID_CONTAINER
        }
    }

    override fun onAttached() {
        super.onAttached()
        launchWithProgressBar {
            val simpleViewNode = pageViewModel.serverApi.screenComponents()
            rebuildContent(simpleViewNode)
        }
    }

    private fun rebuildContent(node: ScreenNode) {
        container.clear()
        var i = 0
        container.append(document.create.div {
            node.forEach {
                div(classes = i.evenOddStyle) {
                    style = "padding-left:calc(var(--tree-offset-left) * ${it.level});"
                    span(classes = CSS_PACKAGE_NAME) { text(it.name.substringBeforeLast(".") + ".") }
                    span(classes = CSS_TYPE_NAME) { text(it.name.substringAfterLast(".").substringBefore("@")) }
                    span(classes = CSS_REF) { text(it.name.substringAfterLast("@")) }
                }
                i++
            }
        })
    }

    private val Int.evenOddStyle get() = if (this % 2 == 0) CSS_ROW_EVEN else CSS_ROW_ODD
}