package com.scurab.uitor.web.inspector

import com.scurab.uitor.web.model.ViewNode
import com.scurab.uitor.web.ui.ColumnsLayout
import com.scurab.uitor.web.util.requireElementById
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.asDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlin.browser.document
import kotlin.browser.window
import kotlin.dom.clear
import kotlin.js.Json

private const val ID_LEFT = "split-table-left"
private const val ID_MID = "split-table-mid"
private const val ID_RIGHT = "split-table-right"

class LayoutInspectorPage {
    private val root = document.requireElementById("root")
    private lateinit var columnsLayout: ColumnsLayout
    private lateinit var canvasView: CanvasView
    private lateinit var treeView: TreeView

    fun onStart() {
        root.clear()
        columnsLayout = ColumnsLayout(root).attach()
        canvasView = CanvasView(columnsLayout.left)
        treeView = TreeView(columnsLayout.middle.first())

        GlobalScope.launch {
            async {
                val item = ViewNode(load())
                canvasView.renderMouseCross = true
                canvasView.root = item
                treeView.root = item
            }
            async {
                canvasView.loadImage("http://anuitor.scurab.com/demo/sampledata/screen.png?time=1570823462")
            }
        }
    }

    private suspend fun load(): Json {
        val text = window
            .fetch("viewhierarchy.json")
            .asDeferred()
            .await()
            .text()
            .asDeferred()
            .await()

        println(text)
        return JSON.parse(text)
    }
}