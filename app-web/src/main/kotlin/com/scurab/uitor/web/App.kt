package com.scurab.uitor.web

import com.scurab.uitor.common.util.elog
import com.scurab.uitor.common.util.ref
import com.scurab.uitor.web.common.Navigation
import com.scurab.uitor.web.common.ServerApi
import com.scurab.uitor.web.inspector.LayoutInspectorPage
import com.scurab.uitor.web.model.ClientConfig
import com.scurab.uitor.web.model.PageViewModel
import com.scurab.uitor.web.resources.ResourcesPage
import com.scurab.uitor.web.threed.ThreeDPage
import com.scurab.uitor.web.tree.TidyTreePage
import com.scurab.uitor.web.ui.PageProgressBar
import com.scurab.uitor.web.ui.launchWithProgressBar
import com.scurab.uitor.web.util.HashToken
import kotlinx.coroutines.GlobalScope
import kotlin.browser.document
import kotlin.browser.window

fun main() {
    App.start()
}

object App {
    val serverApi = ServerApi()
    lateinit var clientConfig: ClientConfig; private set

    fun start() {
        document.body.ref.firstElementChild.ref.let {
            PageProgressBar.attachTo(it)
        }
        GlobalScope.launchWithProgressBar {
            try {
                clientConfig = serverApi.clientConfiguration()
                openPageBaseOnUrl()
            } catch (e: Throwable) {
                window.alert("Unable to load client configuration")
                elog("App") { e.message ?: "Null message" }
            }
        }
    }

    fun openPageBaseOnUrl() {
        val token = HashToken(window.location.hash)
        val screenIndex = token.screenIndex?.toInt() ?: 0
        val page = when (token.pageId) {
            "TidyTreePage" -> TidyTreePage(PageViewModel(screenIndex, clientConfig, serverApi))
            "LayoutInspectorPage" -> LayoutInspectorPage(PageViewModel(screenIndex, clientConfig, serverApi))
            "ThreeDPage" -> ThreeDPage(PageViewModel(screenIndex, clientConfig, serverApi))
            "ResourcesPage" -> ResourcesPage(PageViewModel(screenIndex, clientConfig, serverApi))
            else -> MainPage(clientConfig)
        }
        Navigation.open(page)
    }
}