package com.scurab.uitor.web

import com.scurab.uitor.common.util.ise
import com.scurab.uitor.common.util.ref
import com.scurab.uitor.web.common.Page
import com.scurab.uitor.web.common.ServerApi
import com.scurab.uitor.web.filebrowser.FileBrowserPage
import com.scurab.uitor.web.groovy.GroovyPage
import com.scurab.uitor.web.inspector.LayoutInspectorPage
import com.scurab.uitor.web.model.ClientConfig
import com.scurab.uitor.web.model.IClientConfig
import com.scurab.uitor.web.model.PageViewModel
import com.scurab.uitor.web.model.Pages
import com.scurab.uitor.web.model.Snapshot
import com.scurab.uitor.web.resources.ResourcesPage
import com.scurab.uitor.web.screen.ScreenComponentsPage
import com.scurab.uitor.web.threed.ThreeDPage
import com.scurab.uitor.web.tree.TidyTreePage
import com.scurab.uitor.web.ui.launchWithProgressBar
import com.scurab.uitor.web.util.Browser
import com.scurab.uitor.web.util.lazyLifecycled
import com.scurab.uitor.web.util.readAsText
import com.scurab.uitor.web.util.removeAll
import com.scurab.uitor.web.util.requireElementById
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.asDeferred
import kotlinx.coroutines.async
import kotlinx.html.HtmlBlockTag
import kotlinx.html.InputType
import kotlinx.html.TABLE
import kotlinx.html.button
import kotlinx.html.div
import kotlinx.html.id
import kotlinx.html.input
import kotlinx.html.js.onChangeFunction
import kotlinx.html.js.onClickFunction
import kotlinx.html.js.option
import kotlinx.html.select
import kotlinx.html.style
import kotlinx.html.table
import kotlinx.html.td
import kotlinx.html.tr
import kotlinx.html.unsafe
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.HTMLSelectElement
import org.w3c.dom.get
import kotlinx.browser.window
import kotlinx.dom.clear

private const val ID_SCREEN_INDEX = "main-screen-index"
private const val DEVICE_INFO = "main-screen-device-info"

class MainPage(private var clientConfig: IClientConfig) : Page() {

    override var element: HTMLElement? = null
        private set

    private val screensSelect by lazyLifecycled { element.ref.requireElementById<HTMLSelectElement>(ID_SCREEN_INDEX) }
    private val serverApi get() = App.serverApi
    private val screenIndexOptional: Int?
        get() = screensSelect.selectedIndex.takeIf { it >= 0 }
    private val screenIndex: Int
        get() = screenIndexOptional ?: ise("Invalid screen selection")

    override fun buildContent() {
        //let anyone who is bound to this to reset the state
        onDetachObservable.post(this)
        val el = element ?: document.create.div()
        element = el
        el.clear()
        document.create.div {
            style = "text-align: center; padding:10px;"
            div(classes = DEVICE_INFO) {
                unsafe { raw(clientConfig.deviceInfo.replace("\n", "<br/>")) }
            }
            select {
                id = ID_SCREEN_INDEX
            }
            table {
                style = "margin-left:auto;margin-right:auto;"
                createPageButton(Pages.LayoutInspector, "Layout Inspector")
                { LayoutInspectorPage(PageViewModel(screenIndex)) }
                createPageButton(Pages.ThreeD, "3D Inspector")
                { ThreeDPage(PageViewModel(screenIndex)) }
                createPageButton(Pages.TidyTree, "View Hierarchy")
                { TidyTreePage(PageViewModel(screenIndex)) }
                createPageButton(Pages.Resources, "Resources", false)
                { ResourcesPage(PageViewModel(screenIndexOptional ?: -1)) }
                createPageButton(Pages.FileBrowser, "File Browser", true)
                { FileBrowserPage(PageViewModel(screenIndexOptional ?: -1)) }
                createPageButton(Pages.Windows, "Windows") {
                    ScreenComponentsPage(
                        PageViewModel(
                            screenIndexOptional ?: -1
                        )
                    )
                }
                createLinkButton(Pages.WindowsDetailed, "Windows Detailed") { App.serverApi.screenStructureUrl() }
                createLinkButton(Pages.Screenshot, "Screenshot") { App.serverApi.screenShotUrl(screenIndex) }
                createLinkButton(Pages.LogCat, "LogCat") { App.serverApi.logCatUrl() }
                createPageButton(Pages.Groovy, "Groovy", true) {
                    GroovyPage(
                        PageViewModel(screenIndexOptional ?: -1),
                        null
                    )
                }
                tr {
                    td {
                        div {
                            style = "display: grid;grid-template-columns: 1fr 5.6px 1fr;"
                            button {
                                id = "Save"
                                text("Save")
                                onClickFunction = Browser.saveButtonHandler(clientConfig, serverApi) { screenIndex }
                            }
                            div {}
                            createLoadButton()
                        }
                    }
                }

                val savingForDemo = false
                if(savingForDemo) {
                    tr { td { button {
                                text("DemoSave")
                                onClickFunction = {
                                    launchWithProgressBar {
                                        async { window.fetch(ServerApi.URL_RESOURCES_ALL).asDeferred() }.await()
                                        serverApi.loadResourceItem().forEach { (k, v) ->
                                            v.forEach { res ->
                                                serverApi.loadResourceItem(0, res.key)
                                            }
                                        }
                                    }
                                }
                    } } }
                }
            }
        }.apply {
            el.append(this)
        }
    }

    override fun onAttached() {
        super.onAttached()
        reloadScreens()
    }

    override fun stateDescription(): String? = null

    private fun reloadScreens() {
        if (clientConfig == ClientConfig.FallBackConfig) {
            //failcase, just keep showing Load
            val elems = arrayOf(screensSelect, (document.getElementById("Save") as? HTMLElement))
            elems.forEach {
                it?.style?.visibility = "hidden"
            }
            return
        }
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


    private fun TABLE.createPageButton(
        key: String,
        title: String,
        demoDisabled: Boolean = false,
        block: () -> Page
    ) {
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
                val url = block()
                when {
                    url.startsWith("data:image") -> Browser.openImageInNewTab(url)
                    url.startsWith("data:text") -> Browser.openTextDataInNewTab(url)
                    else -> window.open(url, "_blank", "")
                }
            } catch (e: Throwable) {
                alert(e)
            }
        }
    }

    private fun TABLE.createButton(key: String, title: String, clickAction: (HTMLButtonElement) -> Unit) {
        if (key.isNotEmpty() && !clientConfig.pages.contains(key)) {
            return
        }
        tr {
            td {
                button {
                    style = "width:100%"
                    text(title)
                    onClickFunction = { clickAction(it.target as HTMLButtonElement) }
                }
            }
        }
    }

    private fun HtmlBlockTag.createLoadButton() {
        button {
            onClickFunction = {
                ((it.target as HTMLElement).children[0] as HTMLElement).click()
            }
            text("Load")
            input(classes = "inputfile") {
                type = InputType.file
                accept = Browser.CONTENT_JSON
                onChangeFunction = { event ->
                    val files = (event.target as? HTMLInputElement)?.files
                    val file = files
                        ?.takeIf { it.length > 0 }
                        ?.item(0)
                        ?.let { file ->
                            GlobalScope.launchWithProgressBar {
                                val snapshot = JSON.parse<Snapshot>(file.readAsText())
                                App.setSnapshot(snapshot)
                                clientConfig = App.clientConfig
                                buildContent()
                                reloadScreens()
                            }
                        }
                }
            }
        }
    }
}