package com.scurab.uitor.web

import com.scurab.uitor.common.util.ise
import com.scurab.uitor.common.util.ref
import com.scurab.uitor.web.common.Navigation
import com.scurab.uitor.web.common.Page
import com.scurab.uitor.web.common.ServerApi
import com.scurab.uitor.web.inspector.LayoutInspectorPage
import com.scurab.uitor.web.model.ClientConfig
import com.scurab.uitor.web.model.PageViewModel
import com.scurab.uitor.web.threed.ThreeDPage
import com.scurab.uitor.web.tree.TidyTreePage
import com.scurab.uitor.web.util.removeAll
import com.scurab.uitor.web.util.requireElementById
import kotlinx.coroutines.launch
import kotlinx.html.TABLE
import kotlinx.html.button
import kotlinx.html.div
import kotlinx.html.id
import kotlinx.html.js.onClickFunction
import kotlinx.html.js.option
import kotlinx.html.select
import kotlinx.html.style
import kotlinx.html.table
import kotlinx.html.td
import kotlinx.html.tr
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLSelectElement

private const val ID_SCREEN_INDEX = "main-screen-index"
private const val DEVICE_INFO = "main-screen-device-info"

class MainPage(private val clientConfig: ClientConfig) : Page() {

    override var element: HTMLElement? = null
        private set

    private val screensSelect by lazy { element.ref.requireElementById<HTMLSelectElement>(ID_SCREEN_INDEX) }
    private val serverApi = ServerApi()
    private val selectedScreenIndex: Int
        get() = screensSelect.selectedIndex.takeIf { it >= 0 } ?: ise("Invalid screen selection")

    override fun buildContent() {
        element = document.create.div {
            style = "text-align: center; padding:10px;"
            div(classes = DEVICE_INFO) {
                text(clientConfig.deviceInfo)
            }
            select {
                id = ID_SCREEN_INDEX
            }
            table {
                style = "margin-left:auto;margin-right:auto;"
                createButton("Layout Inspector") { LayoutInspectorPage(PageViewModel(selectedScreenIndex)) }
                createButton("3D Inspector") { ThreeDPage(PageViewModel(selectedScreenIndex)) }
                createButton("View Hierarchy") { TidyTreePage(PageViewModel(selectedScreenIndex)) }
            }
        }
    }

    override fun onAttached() {
        super.onAttached()
        reloadScreens()
    }

    override fun stateDescription(): String? = null

    private fun reloadScreens() {
        launch {
            screensSelect.disabled = true
            screensSelect.options.removeAll()
            val activeScreens = serverApi.activeScreens()
            activeScreens.forEach {
                screensSelect.add(document.create.option { text(it) })
            }
            screensSelect.selectedIndex = activeScreens.size - 1
            screensSelect.disabled = false
        }
    }

    private fun TABLE.createButton(title: String, block: () -> Page) {
        tr {
            td {
                button {
                    style = "width:100%"
                    text(title)
                    onClickFunction = {
                        try {
                            Navigation.open(block())
                        } catch (e: Throwable) {
                            alert(e)
                        }
                    }
                }
            }
        }
    }
}