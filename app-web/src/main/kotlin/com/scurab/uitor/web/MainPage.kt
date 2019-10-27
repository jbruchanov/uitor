package com.scurab.uitor.web

import com.scurab.uitor.common.util.isa
import com.scurab.uitor.common.util.ref
import com.scurab.uitor.web.inspector.LayoutInspectorPage
import com.scurab.uitor.web.model.PageViewModel
import com.scurab.uitor.web.tree.TidyTreePage
import com.scurab.uitor.web.util.lazyLifecycled
import com.scurab.uitor.web.util.removeAll
import com.scurab.uitor.web.util.requireElementById
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.html.TABLE
import kotlinx.html.button
import kotlinx.html.div
import kotlinx.html.dom.create
import kotlinx.html.id
import kotlinx.html.js.onClickFunction
import kotlinx.html.js.option
import kotlinx.html.select
import kotlinx.html.table
import kotlinx.html.td
import kotlinx.html.tr
import org.w3c.dom.Element
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLSelectElement
import kotlin.browser.document
import kotlin.browser.window

private const val ID_SCREEN_INDEX = "main-screen-index"

class MainPage : Page() {
    override var element: HTMLElement? = null
        private set

    private val screensSelect by lazyLifecycled { element.ref.requireElementById<HTMLSelectElement>(ID_SCREEN_INDEX) }
    private val serverApi = ServerApi()
    private val selectedScreenIndex: Int
        get() = screensSelect.selectedIndex.takeIf { it >= 0 } ?: isa("Invalid screen selection")

    override fun buildContent() {
        element = document.create.div {
            table {
                select {
                    id = ID_SCREEN_INDEX
                }
                createButton("LayoutInspector") { LayoutInspectorPage(PageViewModel(selectedScreenIndex)) }
                createButton("View Hierarchy") { TidyTreePage(PageViewModel(selectedScreenIndex)) }
            }
        }
    }

    override fun onAttachToRoot(rootElement: Element) {
        super.onAttachToRoot(rootElement)
        reloadScreens()
    }

    private fun reloadScreens() {
        GlobalScope.launch {
            screensSelect.disabled = true
            screensSelect.options.removeAll()
            val activeScreens = serverApi.activeScreens()
            activeScreens.forEach {
                screensSelect.add(document.create.option { text(it) })
            }
            screensSelect.disabled = false
        }
    }

    private fun TABLE.createButton(title: String, block: () -> Page) {
        tr {
            td {
                button {
                    text(title)
                    onClickFunction = {
                        try {
                            Navigation.open(block())
                        } catch (e: Throwable) {
                            window.alert(e.message ?: "Null error message :(")
                        }
                    }
                }
            }
        }
    }
}