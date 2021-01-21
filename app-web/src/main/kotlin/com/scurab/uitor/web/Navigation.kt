package com.scurab.uitor.web

import com.scurab.uitor.common.util.iae
import com.scurab.uitor.common.util.ilog
import com.scurab.uitor.web.common.Page
import com.scurab.uitor.web.filebrowser.FileBrowserPage
import com.scurab.uitor.web.groovy.GroovyPage
import com.scurab.uitor.web.inspector.InspectorViewModel
import com.scurab.uitor.web.inspector.LayoutInspectorPage
import com.scurab.uitor.web.inspector.ViewPropertyPage
import com.scurab.uitor.web.model.PageViewModel
import com.scurab.uitor.web.resources.ResourcesPage
import com.scurab.uitor.web.screen.ScreenComponentsPage
import com.scurab.uitor.web.threed.ThreeDPage
import com.scurab.uitor.web.tree.TidyTreePage
import com.scurab.uitor.web.ui.PageProgressBar
import com.scurab.uitor.web.util.HashToken
import com.scurab.uitor.web.util.requireElementById
import org.w3c.dom.HashChangeEvent
import org.w3c.dom.events.Event
import kotlinx.browser.document
import kotlinx.browser.window

object Navigation {
    private const val TAG = "Navigation"
    private val rootElement = document.requireElementById("root")
    private var currentPage: Page? = null

    init {
        window.addEventListener("hashchange", { e: Event ->
            val ev = e as HashChangeEvent
            val from = HashToken(ev.oldURL)
            val to = HashToken(ev.newURL)
            ilog(TAG) { "HashChanged from:'${from.pageId}' to:'${to.pageId}'" }
            when {
                currentPage?.pageId == to.pageId -> currentPage?.onHashTokenChanged(from, to)
                else -> openPageBaseOnUrl()
            }
        })
    }

    fun open(page: Page, pushState: Boolean = false) {
        PageProgressBar.hide(-1)
        currentPage?.detach()
        if (pushState) {
            window.history.pushState(
                page.pageId,
                page.pageId,
                page.hashTokenValue()
            )
        }
        currentPage = page
        page.attachTo(rootElement)
    }

    fun updateDescriptionState(page: Page) {
        window.history.replaceState(page.pageId, page.pageId, page.hashTokenValue())
    }

    private fun Page.hashTokenValue(): String {
        val state = stateDescription()?.let { "${HashToken.DELIMITER}$it" } ?: ""
        return "${HashToken.HASH}${pageId}$state"
    }


    fun buildUrl(page: String, vararg keyvalue: Pair<String, Any>): String {
        return StringBuilder()
            .append(HashToken.HASH)
            .append(page)
            .apply {
                keyvalue.forEach { (key, value) ->
                    append(HashToken.DELIMITER)
                    append(key)
                    append("=")
                    append(value)
                }
            }
            .toString()
    }

    fun openPageBaseOnUrl() {
        pageBaseOnUrl()?.let { open(it) }
    }

    private fun pageBaseOnUrl(): Page? {
        val token = HashToken()
        val screenIndex = token.screenIndex?.toInt() ?: 0
        val pageViewModel = PageViewModel(screenIndex, App.clientConfig, App.serverApi)
        return when (token.pageId) {
            "TidyTreePage" -> TidyTreePage(pageViewModel)
            "LayoutInspectorPage" -> LayoutInspectorPage(pageViewModel)
            "ThreeDPage" -> ThreeDPage(pageViewModel)
            "ResourcesPage" -> ResourcesPage(pageViewModel)
            "FileBrowserPage" -> {
                ensureNotDemo()
                FileBrowserPage(pageViewModel)
            }
            "ScreenComponentsPage" -> ScreenComponentsPage(pageViewModel)
            "ViewPropertyPage" -> {
                ensureNotDemo()
                val position = token.arguments["position"]?.toIntOrNull() ?: iae("Missing 'position' arg")
                val property = token.arguments["property"] ?: iae("Missing 'property' arg")
                ViewPropertyPage(position, property, InspectorViewModel(pageViewModel))
            }
            "GroovyPage" -> {
                ensureNotDemo()
                GroovyPage(PageViewModel(screenIndex), token.arguments["position"]?.toIntOrNull())
            }
            else -> MainPage(App.clientConfig)
        }
    }

    private fun ensureNotDemo() {
        check(!DEMO) { "Sorry this page is not supported in demo" }
    }
}