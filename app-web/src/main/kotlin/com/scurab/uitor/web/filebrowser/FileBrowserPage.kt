package com.scurab.uitor.web.filebrowser

import com.scurab.uitor.common.util.iae
import com.scurab.uitor.common.util.ref
import com.scurab.uitor.web.common.Navigation
import com.scurab.uitor.web.common.Page
import com.scurab.uitor.web.filebrowser.FSTableItem.Companion.TYPE_FILE
import com.scurab.uitor.web.filebrowser.FSTableItem.Companion.TYPE_FOLDER
import com.scurab.uitor.web.filebrowser.FSTableItem.Companion.TYPE_PARENT_FOLDER
import com.scurab.uitor.web.model.FSItem
import com.scurab.uitor.web.model.PageViewModel
import com.scurab.uitor.web.ui.launchWithProgressBar
import com.scurab.uitor.web.ui.table.ITableDataItem
import com.scurab.uitor.web.ui.table.TableData
import com.scurab.uitor.web.ui.table.TableView
import com.scurab.uitor.web.ui.table.TableViewDelegate
import com.scurab.uitor.web.util.lazyLifecycled
import com.scurab.uitor.web.util.requireElementById
import kotlinx.html.div
import kotlinx.html.id
import kotlinx.html.js.div
import kotlinx.html.span
import org.w3c.dom.Element
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLSpanElement
import kotlin.browser.window

private const val ID_FILE_BROWSER_CONTAINER = "file-browser-container"
private const val ID_FILE_BROWSER_CURRENT_FOLDER = "file-browser-current-folder"

class FileBrowserPage(private val pageViewModel: PageViewModel) : Page() {

    private var _stateDescription: String? = null
        set(value) {
            field = value
            Navigation.updateDescriptionState(this)
        }
    override fun stateDescription(): String? = "path=$_stateDescription"

    override var element: HTMLElement? = null; private set
    private val parentFolder = FSItem("..", "", TYPE_PARENT_FOLDER)
    private val currentFolder by lazyLifecycled {
        element.ref.requireElementById<HTMLSpanElement>(
            ID_FILE_BROWSER_CURRENT_FOLDER
        )
    }

    private val tableView = TableView(delegate = TableViewDelegate<FSTableItem>().apply {
        cellClickListener = { item, row, column -> onItemClick(item) }
    })
    private var remotePath: String = ""
        set(value) {
            field = value
            _stateDescription = value
        }

    override fun buildContent() {
        element = document.create.div {
            id = ID_FILE_BROWSER_CONTAINER
            div {
                span { text("Location:") }
                span {
                    id = ID_FILE_BROWSER_CURRENT_FOLDER
                }
            }
        }
    }

    override fun onAttachToRoot(rootElement: Element) {
        super.onAttachToRoot(rootElement)
        tableView.attachTo(element.ref)
        loadPath("")
    }

    private fun onItemClick(tableItem: FSTableItem) {
        val item = tableItem.item
        when (item.type) {
            TYPE_FILE -> openFile(item)
            TYPE_PARENT_FOLDER -> loadPath(remotePath.substringBeforeLast("/"))
            TYPE_FOLDER -> loadPath(remotePath.joinWith(item.name))
            else -> iae("Unsupported item.type:${item.type}")
        }
    }

    private fun openFile(item: FSItem) {
        val loc = window.location
        val path = remotePath.joinWith(item.name)
        val url = "${loc.protocol}//${loc.host}/storage.json?path=$path"
        window.open(url, "_blank", "")
    }

    private fun loadPath(path: String) {
        remotePath = path
        currentFolder.innerText = path
        launchWithProgressBar {
            val items = pageViewModel.serverApi.loadFileStorage(path).toMutableList()
            if (path.isNotEmpty()) {
                items.add(0, parentFolder)
            }
            tableView.data = TableData(
                headers = arrayOf("Name", "Size"),
                initElements = items.map { FSTableItem(it) }
            )
        }
    }

    private fun String.joinWith(path: String): String {
        val addSep = !(this.endsWith("/") || path.startsWith("/"))
        var result = this
        if (addSep) {
            result += "/"
        }
        result += path
        return result
    }
}

private class FSTableItem(val item: FSItem) : ITableDataItem {
    override fun get(column: Int): Any {
        return when (column) {
            0 -> item.name
            1 -> when (item.type) {
                TYPE_PARENT_FOLDER -> ""
                TYPE_FILE -> item.size
                TYPE_FOLDER -> "DIR"
                else -> iae("Invalid type:${item.type}")
            }
            else -> iae("Invalid column:$column")
        }
    }

    override val tableColumns: Int = 2

    companion object {
        var TYPE_PARENT_FOLDER = -1
        var TYPE_FILE = 1
        var TYPE_FOLDER = 2
    }
}