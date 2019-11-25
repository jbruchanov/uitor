package com.scurab.uitor.web

import com.scurab.uitor.common.util.ise
import com.scurab.uitor.common.util.ref
import com.scurab.uitor.web.common.Page
import com.scurab.uitor.web.common.ServerApi
import com.scurab.uitor.web.filebrowser.FileBrowserPage
import com.scurab.uitor.web.groovy.GroovyPage
import com.scurab.uitor.web.inspector.LayoutInspectorPage
import com.scurab.uitor.web.model.ClientConfig
import com.scurab.uitor.web.model.PageViewModel
import com.scurab.uitor.web.model.ViewNode
import com.scurab.uitor.web.resources.ResourcesPage
import com.scurab.uitor.web.screen.ScreenComponentsPage
import com.scurab.uitor.web.threed.ThreeDPage
import com.scurab.uitor.web.tree.TidyTreePage
import com.scurab.uitor.web.ui.launchWithProgressBar
import com.scurab.uitor.web.util.browserDownload
import com.scurab.uitor.web.util.loadImage
import com.scurab.uitor.web.util.obj
import com.scurab.uitor.web.util.removeAll
import com.scurab.uitor.web.util.requireElementById
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
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
import kotlin.browser.window
import kotlin.js.Json

private const val ID_SCREEN_INDEX = "main-screen-index"
private const val DEVICE_INFO = "main-screen-device-info"

class MainPage(private val clientConfig: ClientConfig) : Page() {

    override var element: HTMLElement? = null
        private set

    private val screensSelect by lazy { element.ref.requireElementById<HTMLSelectElement>(ID_SCREEN_INDEX) }
    private val serverApi = ServerApi()
    private val screenIndexOptional: Int?
        get() = screensSelect.selectedIndex.takeIf { it >= 0 }
    private val screenIndex: Int
        get() = screenIndexOptional ?: ise("Invalid screen selection")

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
                { LayoutInspectorPage(PageViewModel(screenIndex)) }
                createPageButton("ThreeDPage", "3D Inspector")
                { ThreeDPage(PageViewModel(screenIndex)) }
                createPageButton("TidyTreePage", "View Hierarchy")
                { TidyTreePage(PageViewModel(screenIndex)) }
                createPageButton("ResourcesPage", "Resources", true)
                { ResourcesPage(PageViewModel(screenIndexOptional ?: -1)) }
                createPageButton("FileBrowserPage", "File Browser", true)
                { FileBrowserPage(PageViewModel(screenIndexOptional ?: -1)) }
                createPageButton("WindowsPage", "Windows") {
                    ScreenComponentsPage(
                        PageViewModel(
                            screenIndexOptional ?: -1
                        )
                    )
                }
                createLinkButton("WindowsDetailedPage", "Windows Detailed") { "screenstructure" }
                createLinkButton("ScreenshotPage", "Screenshot") { ServerApi.screenShotUrl(screenIndex) }
                createLinkButton("LogCatPage", "LogCat") { "logcat" }
                createPageButton("GroovyPage", "Groovy", true) {
                    GroovyPage(
                        PageViewModel(screenIndexOptional ?: -1),
                        null
                    )
                }
                createButton("", "Save") {
                    launchWithProgressBar {
                        val screenIndex = screenIndex
                        val screen = serverApi.activeScreens()[screenIndex]

                        val image = async { loadImage(ServerApi.screenShotUrl(screenIndex)) }
                        val viewHierarchy = async { serverApi.rawViewHierarchy(screenIndex) }
                        val clientConfig = async { serverApi.rawClientConfiguration() }

                        val vh = viewHierarchy.await()
                        val viewNode = ViewNode(vh)
                        val cf = clientConfig.await()
                        val im = image.await()
                        val viewShots = viewNode.all()
                            .map { vn ->
                                if (vn.shouldRender) {
                                    loadImage(ServerApi.viewShotUrl(screenIndex, vn.position))
                                } else null
                            }
                        val obj = obj<Snapshot> {
                            this.name = screen
                            this.viewHierarchy = vh
                            this.clientConfiguration = cf
                            this.screenshot = im
                            this.viewShots = viewShots
                        }
                        browserDownload(JSON.stringify(obj), "snapshot.json", "application/json")
                    }
                }
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


    private fun TABLE.createPageButton(key: String, title: String, demoDisabled: Boolean = false, block: () -> Page) {
        createButton(key, title) {
            if (demoDisabled && DEMO) {
                alert("Sorry, $title is not supported in demo!")
                return@createButton
            }
            try {
                Navigation.open(block(), true)
            } catch (e: Throwable) {
                alert(e)
            }
        }
    }

    private fun TABLE.createLinkButton(key: String, title: String, block: () -> String) {
        createButton(key, title) {
            try {
                window.open(block(), "_blank", "")
            } catch (e: Throwable) {
                alert(e)
            }
        }
    }

    private fun TABLE.createButton(key: String, title: String, clickAction: () -> Unit) {
        if (key.isNotEmpty() && !clientConfig.pages.contains(key)) {
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

interface Snapshot {
    var name: String
    var version: String
    var taken: String
    var viewHierarchy: Json
    var clientConfiguration: Json
    var screenshot: String?
    var viewShots: List<String?>
}