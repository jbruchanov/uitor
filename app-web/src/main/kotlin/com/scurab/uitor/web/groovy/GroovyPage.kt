package com.scurab.uitor.web.groovy

import js.ace.Editor
import com.scurab.uitor.common.util.dlog
import com.scurab.uitor.web.common.Page
import com.scurab.uitor.web.model.PageViewModel
import com.scurab.uitor.web.ui.launchWithProgressBar
import com.scurab.uitor.web.util.HashToken
import com.scurab.uitor.web.util.lazyLifecycled
import kotlinx.html.button
import kotlinx.html.div
import kotlinx.html.id
import kotlinx.html.js.div
import kotlinx.html.js.onClickFunction
import kotlinx.html.unsafe
import org.w3c.dom.Element
import org.w3c.dom.HTMLElement
import kotlin.js.Date

private const val CSS_CODE_EDITOR_CONTAINER = "code-editor-container"
private const val ID_EDITOR = "editor"
private const val ID_RESULT = "result"

class GroovyPage(private val viewModel: PageViewModel, private val position: Int?) : Page() {
    override fun stateDescription(): String? =
        position?.let { HashToken.state("screenIndex" to viewModel.screenIndex, "position" to it) }
    private lateinit var editor: Editor
    override var element: HTMLElement? = null; private set

    private val result by lazyLifecycled { requireElementById<HTMLElement>(ID_RESULT) }
    private var waitingForResponse = false

    override fun buildContent() {
        element = document.create.div(classes = CSS_CODE_EDITOR_CONTAINER) {
            div {
                id = ID_EDITOR
                unsafe {
                    val view = position?.let { "def view = G.getView(${viewModel.screenIndex}, $it)" } ?: ""
                    raw("""
                    /*
                    There is no specific package context. Always **use full class names**!
                    
                    Few tips:
                    def viewId = G.id("R.id.button") //get android ID value, don't use directly R.id.button
                    def obj = G.field(myObject, "mMyPrivateField") //get reference to any field of object
                    def view = new android.widget.TextView(activity) //create new TextView
                    
                    */
                    
                    
                    def G = groovy.lang.GroovyHelper
                    def rootView = G.getRootView(0)
                    def app = G.getApplication()
                    def activities = G.getActivities()
                    $view

                    
                    """.trimIndent())
                }
            }
            div {
                button {
                    text("Execute")
                    onClickFunction = { executeCode() }
                }
                button {
                    text("Clear Result")
                    onClickFunction = { clearResult() }
                }
            }
            div {
                id = ID_RESULT
            }
        }
    }

    private fun executeCode() {
        if (waitingForResponse) return
        waitingForResponse = true
        launchWithProgressBar {
            val response = viewModel.serverApi.executeGroovyCode(editor.getValue())
            val now = Date().toLocaleString()
            result.innerText = "$now\n$response\n---------------------------------\n" +
                    result.innerText
            waitingForResponse = false
        }
    }

    private fun clearResult() {
        result.innerText = ""
    }

    override fun onAttachToRoot(rootElement: Element) {
        super.onAttachToRoot(rootElement)
        editor = js.ace.edit(ID_EDITOR).apply {
            setTheme("ace/theme/monokai")
            session.setMode("ace/mode/groovy")
        }

        document.addKeyUpListener {
            dlog { "U" + it.keyCode.toString() + "shift:${it.shiftKey}" }
            if (it.keyCode == 13 /*enter*/) {
                if (it.ctrlKey) {
                    executeCode()
                } else if (it.altKey) {
                    clearResult()
                }
            }
        }
    }
}