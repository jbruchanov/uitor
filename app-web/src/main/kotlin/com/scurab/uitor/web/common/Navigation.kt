package com.scurab.uitor.web.common

import com.scurab.uitor.common.util.ilog
import com.scurab.uitor.web.App
import com.scurab.uitor.web.ui.PageProgressBar
import com.scurab.uitor.web.util.HashToken
import com.scurab.uitor.web.util.requireElementById
import org.w3c.dom.HashChangeEvent
import org.w3c.dom.events.Event
import kotlin.browser.document
import kotlin.browser.window

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
                else -> App.openPageBaseOnUrl()
            }
        })
    }

    fun open(page: Page) {
        open(page, true)
    }

    fun open(page: Page, pushState: Boolean) {
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
}