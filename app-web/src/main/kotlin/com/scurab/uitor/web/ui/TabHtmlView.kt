package com.scurab.uitor.web.ui

import com.scurab.uitor.common.util.ref
import com.scurab.uitor.web.util.requireElementById
import kotlinx.html.button
import kotlinx.html.div
import kotlinx.html.hidden
import kotlinx.html.id
import kotlinx.html.js.div
import kotlinx.html.js.onClickFunction
import org.w3c.dom.Element
import org.w3c.dom.HTMLElement

class TabDataProvider(
    val count: Int,
    val name: (page: Int) -> String,
    val creator: (page: Int) -> HtmlView
)

class TabHtmlView(private val tabDataProvider: TabDataProvider) : HtmlView() {
    override var element: HTMLElement? = null; private set
    private val tabContainers by lazy {
        Array(tabDataProvider.count) { element.ref.requireElementById<HTMLElement>("tab:$it") }
    }

    private lateinit var tabs: Array<HtmlView>

    override fun buildContent() {
        element = document.create.div {
            div {
                for (i in 0 until tabDataProvider.count) {
                    button {
                        text(tabDataProvider.name(i))
                        onClickFunction = { selectTab(i) }
                    }
                }
            }
            for (i in 0 until tabDataProvider.count) {
                div {
                    id = "tab:$i"
                    hidden = i != 0
                }
            }
        }

        tabs = Array(tabDataProvider.count) {
            tabDataProvider.creator(it).apply {
                buildContent()
            }
        }
    }

    open fun selectTab(index: Int) {
        tabContainers.forEachIndexed { i, e ->
            e.hidden = i != index
        }
    }

    override fun onAttachToRoot(rootElement: Element) {
        super.onAttachToRoot(rootElement)
        for (i in 0 until tabDataProvider.count) {
            tabs[i].attachTo(tabContainers[i])
        }
    }

    override fun onAttached() {
        super.onAttached()
        for (i in 0 until tabDataProvider.count) {
            tabs[i].onAttached()
        }
    }
}