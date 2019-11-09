package com.scurab.uitor.web

import com.scurab.uitor.common.util.ise
import com.scurab.uitor.common.util.ref
import com.scurab.uitor.web.common.Navigation
import com.scurab.uitor.web.common.Page
import com.scurab.uitor.web.common.ServerApi
import com.scurab.uitor.web.filebrowser.FileBrowserPage
import com.scurab.uitor.web.groovy.GroovyPage
import com.scurab.uitor.web.inspector.LayoutInspectorPage
import com.scurab.uitor.web.model.ClientConfig
import com.scurab.uitor.web.model.PageViewModel
import com.scurab.uitor.web.resources.ResourcesPage
import com.scurab.uitor.web.screen.ScreenComponentsPage
import com.scurab.uitor.web.threed.ThreeDPage
import com.scurab.uitor.web.tree.TidyTreePage
import com.scurab.uitor.web.ui.launchWithProgressBar
import com.scurab.uitor.web.util.removeAll
import com.scurab.uitor.web.util.requireElementById
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
import kotlin.browser.window

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
                createPageButton("LayoutInspectorPage", "Layout Inspector")
                { LayoutInspectorPage(PageViewModel(selectedScreenIndex)) }
                createPageButton("ThreeDPage", "3D Inspector")
                { ThreeDPage(PageViewModel(selectedScreenIndex)) }
                createPageButton("TidyTreePage", "View Hierarchy")
                { TidyTreePage(PageViewModel(selectedScreenIndex)) }
                createPageButton("ResourcesPage", "Resources")
                { ResourcesPage(PageViewModel(selectedScreenIndex)) }
                createPageButton("FileBrowserPage", "File Browser")
                { FileBrowserPage(PageViewModel(selectedScreenIndex)) }
                createPageButton("WindowsPage", "Windows") { ScreenComponentsPage(PageViewModel(selectedScreenIndex)) }
                createLinkButton("WindowsDetailedPage", "Windows Detailed") { "/screenstructure.json" }
                createLinkButton("ScreenshotPage", "Screenshot") { "/screen.png?screenIndex=${selectedScreenIndex}" }
                createLinkButton("LogCatPage", "LogCat") { "/logcat.txt" }
                createPageButton("GroovyPage", "Groovy") { GroovyPage(PageViewModel(selectedScreenIndex), null) }
            }
        }
    }

    override fun onAttached() {
        super.onAttached()
        reloadScreens()
    }

    override fun stateDescription(): String? = null

    private fun reloadScreens() {
        launchWithProgressBar {
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

    private fun TABLE.createPageButton(key: String, title: String, block: () -> Page) {
        createButton(key, title) {
            try {
                Navigation.open(block())
            } catch (e: Throwable) {
                alert(e)
            }
        }
    }

    private fun TABLE.createLinkButton(key: String, title: String, block: () -> String) {
        createButton(key, title) {
            window.open(block(), "_blank", "")
        }
    }

    private fun TABLE.createButton(key: String, title: String, clickAction: () -> Unit) {
        if (!clientConfig.pages.contains(key)) {
            return
        }
        tr {
            td {
                button {
                    style = "width:100%"
                    text(title)
                    onClickFunction = { clickAction() }
                }
            }
        }
    }
}