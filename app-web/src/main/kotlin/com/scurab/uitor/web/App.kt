package com.scurab.uitor.web

import com.scurab.uitor.common.util.elog
import com.scurab.uitor.common.util.iae
import com.scurab.uitor.common.util.messageSafe
import com.scurab.uitor.common.util.ref
import com.scurab.uitor.web.common.Navigation
import com.scurab.uitor.web.common.ServerApi
import com.scurab.uitor.web.filebrowser.FileBrowserPage
import com.scurab.uitor.web.groovy.GroovyPage
import com.scurab.uitor.web.inspector.InspectorViewModel
import com.scurab.uitor.web.inspector.LayoutInspectorPage
import com.scurab.uitor.web.inspector.ViewPropertyPage
import com.scurab.uitor.web.model.ClientConfig
import com.scurab.uitor.web.model.PageViewModel
import com.scurab.uitor.web.resources.ResourcesPage
import com.scurab.uitor.web.screen.ScreenComponentsPage
import com.scurab.uitor.web.threed.ThreeDPage
import com.scurab.uitor.web.tree.TidyTreePage
import com.scurab.uitor.web.ui.PageProgressBar
import com.scurab.uitor.web.ui.launchWithProgressBar
import com.scurab.uitor.web.util.DocumentWrapper
import com.scurab.uitor.web.util.HashToken
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlin.browser.document
import kotlin.browser.window

fun main() {
    App.start()
}

/*
views have to be renamed to view-position.png => view-0.png
<Directory "/var/www/anuitor/demo2">
   RewriteEngine On
   RewriteCond %{QUERY_STRING} ^position=([0-9]*)
   RewriteRule "^view\.png$" "view-%1.png"
</Directory>
 */
const val DEMO = false

object App {
    val serverApi = ServerApi()
    lateinit var clientConfig: ClientConfig; private set
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
            } catch (e: Throwable) {
                window.alert("Unable to load client configuration")
                elog("App") { e.messageSafe }
                return@launchWithProgressBar
            }
            check(clientConfig.pages.isNotEmpty()) { "ClientConfig.pages is empty!" }
            openPageBaseOnUrl()
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

    fun openPageBaseOnUrl() {
        val token = HashToken(window.location.hash)
        val screenIndex = token.screenIndex?.toInt() ?: 0
        val pageViewModel = PageViewModel(screenIndex, clientConfig, serverApi)
        val page = when (token.pageId) {
            "TidyTreePage" -> TidyTreePage(pageViewModel)
            "LayoutInspectorPage" -> LayoutInspectorPage(pageViewModel)
            "ThreeDPage" -> ThreeDPage(pageViewModel)
            "ResourcesPage" -> ResourcesPage(pageViewModel)
            "FileBrowserPage" -> FileBrowserPage(pageViewModel)
            "ScreenComponentsPage" -> ScreenComponentsPage(pageViewModel)
            "ViewPropertyPage" -> {
                if (DEMO) {
                    window.alert("Sorry ViewPropertyPage is not supported in demo!")
                    return
                }
                val position = token.arguments["position"]?.toIntOrNull() ?: iae("Missing 'position' arg")
                val property = token.arguments["property"] ?: iae("Missing 'property' arg")
                ViewPropertyPage(position, property, InspectorViewModel(pageViewModel))
            }
            "GroovyPage" -> GroovyPage(PageViewModel(0), token.arguments["position"]?.toIntOrNull())
            else -> MainPage(clientConfig)
        }
        Navigation.open(page)
    }
}