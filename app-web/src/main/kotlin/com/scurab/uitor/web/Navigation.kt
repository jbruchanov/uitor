package com.scurab.uitor.web

import App
import com.scurab.uitor.common.util.ilog
import com.scurab.uitor.web.util.HashToken
import com.scurab.uitor.web.util.requireElementById
import org.w3c.dom.HashChangeEvent
import org.w3c.dom.events.Event
import kotlin.browser.document
import kotlin.browser.window

object Navigation {
    private val TAG = "Navigation"
    private val rootElement = document.requireElementById("root")
    private var currentPage: Page? = null

    init {
        window.addEventListener("hashchange", { e: Event ->
            val ev = e as HashChangeEvent
            val from = HashToken(ev.oldURL)
            val to = HashToken(ev.newURL)
            ilog(TAG) { "HashChanged from:'${from.pageId}' to:'${to.pageId}'" }
            when {
                currentPage?.id == to.pageId -> currentPage?.onHashTokenChanged(from, to)
                else -> App.openPageBaseOnUrl()
            }
        })
    }

    fun open(page: Page) {
        open(page, true)
    }

    fun open(page: Page, pushState: Boolean) {
        currentPage?.detach()
        if (pushState) {
            val state = page.stateDescription()?.let { "${HashToken.DELIMITER}$it" } ?: ""
            window.history.pushState(
                page.id,
                page.id,
                "${HashToken.HASH}${page.id}$state"
            )
        }
        currentPage = page
        page.attachTo(rootElement)
    }
}