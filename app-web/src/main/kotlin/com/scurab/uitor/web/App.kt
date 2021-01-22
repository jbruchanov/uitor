package com.scurab.uitor.web

import com.scurab.uitor.common.util.elog
import com.scurab.uitor.common.util.messageSafe
import com.scurab.uitor.common.util.ref
import com.scurab.uitor.web.common.IServerApi
import com.scurab.uitor.web.common.ServerApi
import com.scurab.uitor.web.common.SnapshotServerApi
import com.scurab.uitor.web.model.ClientConfig
import com.scurab.uitor.web.model.IClientConfig
import com.scurab.uitor.web.model.Snapshot
import com.scurab.uitor.web.ui.PageProgressBar
import com.scurab.uitor.web.ui.launchWithProgressBar
import com.scurab.uitor.web.util.DocumentWrapper
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.browser.document
import kotlinx.browser.window

fun main() {
    App.start()
}

const val DEMO = false

object App {
    var serverApi: IServerApi = ServerApi(); private set
    lateinit var clientConfig: IClientConfig; private set
    private var theme: String = ""

    fun start() {
        setTheme(window.localStorage.getItem("theme") ?: "dark")
        document.body.ref.firstElementChild.ref.let {
            PageProgressBar.attachTo(it)
        }
        DocumentWrapper().addKeyUpListener {
            println(it.keyCode)
            if (it.altKey) {
                when (it.keyCode) {
                    84/*t*/ -> switchTheme()
                    82/*r*/ -> reloadTimer()
                }
            }
        }
        GlobalScope.launchWithProgressBar {
            try {
                clientConfig = serverApi.clientConfiguration()
                check(clientConfig.pages.isNotEmpty()) { "ClientConfig.pages are empty!" }
            } catch (e: Throwable) {
                window.alert("Unable to load client configuration")
                elog("App") { e.messageSafe }
                clientConfig = ClientConfig.FallBackConfig
                //no return, just use FallBackConfig to show Load button
            }
            Navigation.openPageBaseOnUrl()
        }
    }

    private fun switchTheme() {
        setTheme(
            if (theme == "dark") {
                "light"
            } else {
                "dark"
            }
        )
    }

    private fun setTheme(theme: String) {
        try {
            window.localStorage.setItem("theme", theme)
        } catch (e: Exception) {
            elog("App") { "Unable to save theme setting into local storage\n${e.messageSafe}" }
        }
        document.documentElement.ref.setAttribute("data-theme", theme)
        App.theme = theme
    }

    private fun reloadTimer() = GlobalScope.launchWithProgressBar(0) {
        delay(3000)
        window.location.reload()
    }

    suspend fun setSnapshot(snapshot: Snapshot) {
        serverApi = SnapshotServerApi(snapshot)
        clientConfig = serverApi.clientConfiguration()
    }
}