package com.scurab.uitor.web.resources

import com.scurab.uitor.common.util.Observable
import com.scurab.uitor.common.util.dlog
import com.scurab.uitor.common.util.iae
import com.scurab.uitor.common.util.ref
import com.scurab.uitor.web.common.Page
import com.scurab.uitor.web.model.PageViewModel
import com.scurab.uitor.web.ui.launchWithProgressBar
import com.scurab.uitor.web.ui.table.IRenderingContext
import com.scurab.uitor.web.ui.table.ITableViewRenderer
import com.scurab.uitor.web.ui.table.TableData
import com.scurab.uitor.web.ui.table.TableView
import com.scurab.uitor.web.ui.table.TableViewDelegate
import com.scurab.uitor.web.ui.table.TextTableViewRenderer
import com.scurab.uitor.web.util.lazyLifecycled
import com.scurab.uitor.web.util.requireElementById
import kotlinx.html.TD
import kotlinx.html.div
import kotlinx.html.id
import kotlinx.html.js.div
import kotlinx.html.js.onClickFunction
import kotlinx.html.span
import kotlinx.html.style
import org.w3c.dom.Element
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLSpanElement
import kotlin.dom.clear

private const val ID_GROUPS_CONTAINER = "resources-groups-container"
private const val ID_GROUPS_TABLE = "resources-groups-table"
private const val ID_ITEMS_CONTAINER = "resources-items-container"
private const val ID_ITEMS_TABLE = "resources-items-table"
private const val ID_CONTENT_CONTAINER = "resources-content-container"
private const val GRID_COLUMNS = "200px 550px 1fr"
private const val DEF_STYLE = "height:100vh;overscroll:auto"

class ResourcesPage(private val pageViewModel: PageViewModel) : Page() {
    private val TAG = "ResourcesPage"
    override fun stateDescription(): String? = null

    override var element: HTMLElement? = null; private set
    private val groupTableDelegate = tableViewDelegate(ID_GROUPS_TABLE, this::onGroupSelected)
    private val itemsTableDelegate = tableViewDelegate(ID_ITEMS_TABLE, this::onItemSelected)
    private val groupTable = TableView(groupTableDelegate)
    private val itemsTable = TableView(itemsTableDelegate)
    private val contentContainer by lazyLifecycled { element.ref.requireElementById<HTMLElement>(ID_CONTENT_CONTAINER) }
    private val resources = Observable<Map<String, List<Triple<Int, String, String?>>>>()
    private val resourcesContentPage = ResourcesContentGenerator()
    private var idNamesToResIds = mutableMapOf<String, Triple<Int, String, String?>>()

    init {
        launchWithProgressBar {
            resources.post(pageViewModel.serverApi.loadResources())
        }
    }

    override fun buildContent() {
        element = document.create.div {
            style = "display: grid;grid-template-columns: $GRID_COLUMNS;"
            div {
                id = ID_GROUPS_CONTAINER
                style = DEF_STYLE
            }
            div {
                id = ID_ITEMS_CONTAINER
                style = DEF_STYLE
            }
            div {
                id = ID_CONTENT_CONTAINER
                style = DEF_STYLE
            }
        }
    }

    override fun onAttachToRoot(rootElement: Element) {
        super.onAttachToRoot(rootElement)
        element?.apply {
            groupTable.attachTo(requireElementById<HTMLElement>(ID_GROUPS_CONTAINER))
            itemsTable.attachTo(requireElementById<HTMLElement>(ID_ITEMS_CONTAINER))
        }

        resources.observe { resources ->
            resources.values.flatten().forEach {
                val v = it.second
                idNamesToResIds[v] = it
            }
            groupTableDelegate.data = TableData(
                arrayOf("Group"),
                resources.keys.sorted().map { arrayOf(it) }
            )
        }
    }

    private fun onGroupSelected(group: String) {
        dlog(TAG) { "Group:$group" }
        val list = resources.item.ref[group] ?: iae("Unable to find group:'${group}'?!")
        itemsTableDelegate.data = TableData(
            arrayOf("Item"),
            list.map { arrayOf(it.second) }
        )
        itemsTable.refreshContent()
    }

    private fun onItemSelected(item: String) {
        val resTuple = idNamesToResIds[item]
        dlog(TAG) { "Item:$item resId:${resTuple?.first}" }
        resTuple?.let {
            contentContainer.clear()
            launchWithProgressBar {
                val item = pageViewModel.serverApi.loadResources(it.first)
                item.source = resTuple.third
                val element = resourcesContentPage.buildContent(item)
                contentContainer.append(element)
                pr.prettyPrint()
            }
        }
    }

    private fun tableViewDelegate(id: String, clickAction: (String) -> Unit): TableViewDelegate<String> {
        return TableViewDelegate(TableData.empty(), tableRenderer(clickAction)).apply {
            elementId = id
            sorting = false
            filtering = false
            selecting = true
        }
    }

    private fun tableRenderer(clickAction: (String) -> Unit): ITableViewRenderer<String> =
        object : TextTableViewRenderer<String>() {
            override val cell: TD.(IRenderingContext<String>, String) -> Unit = { _, value ->
                span {
                    style = "cursor: pointer; display: block; padding: 2px 5px"
                    onClickFunction = { ev ->
                        val innerText = (ev.target as? HTMLSpanElement).ref.innerText
                        clickAction(innerText)
                    }
                    text(value)
                }
            }
        }
}