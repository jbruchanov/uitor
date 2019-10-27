package com.scurab.uitor.web

import com.scurab.uitor.web.common.Navigation
import com.scurab.uitor.web.common.ServerApi
import com.scurab.uitor.web.inspector.LayoutInspectorPage
import com.scurab.uitor.web.model.ClientConfig
import com.scurab.uitor.web.model.PageViewModel
import com.scurab.uitor.web.tree.TidyTreePage
import com.scurab.uitor.web.util.HashToken
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.browser.window

fun main() {
    App.start()
}

object App {
    val serverApi = ServerApi()
    lateinit var clientConfig: ClientConfig; private set

    fun start() {
        try {
            GlobalScope.launch {
                clientConfig = serverApi.clientConfiguration()
                openPageBaseOnUrl()
            }
        } catch (e: Throwable) {
            window.alert("Unable to load client configuration\n${e.message}")
        }
    }

    fun openPageBaseOnUrl() {
        val token = HashToken(window.location.hash)
        val screenIndex = token.screenIndex?.toInt() ?: 0
        val page = when (token.pageId) {
            "TidyTreePage" -> TidyTreePage(PageViewModel(screenIndex, clientConfig, serverApi))
            "LayoutInspectorPage" -> LayoutInspectorPage(PageViewModel(screenIndex, clientConfig, serverApi))
            else -> MainPage()
        }
        Navigation.open(page)
    }
}