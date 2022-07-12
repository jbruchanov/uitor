@file:Suppress("MemberVisibilityCanBePrivate")

package com.scurab.uitor.web.ui.table

import com.scurab.uitor.common.util.ref
import com.scurab.uitor.web.ui.HtmlView
import com.scurab.uitor.web.util.lazyLifecycled
import com.scurab.uitor.web.util.requireElementById
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import kotlinx.html.FlowOrInteractiveOrPhrasingContent
import kotlinx.html.TABLE
import kotlinx.html.div
import kotlinx.html.id
import kotlinx.html.js.div
import kotlinx.html.js.onClickFunction
import kotlinx.html.js.onKeyUpFunction
import kotlinx.html.js.table
import kotlinx.html.table
import kotlinx.html.td
import kotlinx.html.textInput
import kotlinx.html.tfoot
import kotlinx.html.th
import kotlinx.html.thead
import kotlinx.html.tr
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLInputElement
import kotlinx.dom.addClass
import kotlinx.dom.clear
import kotlinx.dom.removeClass

open class TableView<T : ITableDataItem>(
    data: ITableData<T> = TableData.empty(),
    private val delegate: ITableViewDelegate<T> = TableViewDelegate()
) : HtmlView() {
    var data: ITableData<T> = data
        set(value) {
            field = value
            refreshContent()
        }
    override val element: HTMLElement? get() = _element

    protected val tableViewContainer by lazyLifecycled { _element.ref.requireElementById<HTMLElement>(ID_TABLE_CONTAINER) }
    protected var sorting: Sorting? = null; private set
    protected var filterValue: String? = null; private set

    private var _element: HTMLElement? = null
    private var filterChannel = ConflatedBroadcastChannel("")
    private val renderingContext = RenderingContext<T>()
    private var selectedElement: HTMLElement? = null

    override fun buildContent() {
        _element = document.create.div {
            div {
                if (delegate.filtering) {
                    table(classes = CSS_TABLE_VIEW_FILTER) {
                        tr { td { filterInput() } }
                    }
                }
            }
            div(classes = ID_TABLE_CONTAINER) {
                id = ID_TABLE_CONTAINER
            }
        }
        refreshContent()
    }

    override fun onAttached() {
        super.onAttached()
        filterChannel = ConflatedBroadcastChannel("")
        launch {
            filterChannel.asFlow().debounce(200).collect {
                data.filter(it)
                filterValue = it
                refreshContent()
            }
        }
    }

    override fun onDetached() {
        super.onDetached()
        filterChannel.close()
    }

    open fun refreshContent() {
        tableViewContainer.ref.clear()
        document.create.table(classes = CSS_TABLE_VIEW) {
            delegate.elementId?.let { id = it }
            header()
            body()
            footer()
        }.apply {
            tableViewContainer.ref.append(this)
        }
    }

    protected open fun onHeaderClick(column: Int) {
        sorting = Sorting.toggleSort(column, sorting).apply {
            sort(data)
        }
        refreshContent()
    }

    protected open fun onFilterKeyUp(filter: String) {
        filterChannel.trySend(filter.trim())
    }

    private fun FlowOrInteractiveOrPhrasingContent.filterInput() {
        textInput(classes = CSS_TABLE_VIEW_FILTER_INPUT) {
            onKeyUpFunction = {
                onFilterKeyUp((it.currentTarget as HTMLInputElement).value)
            }
        }
    }

    private fun TABLE.header() {
        delegate.render.header?.let { renderer ->
            thead {
                tr {
                    for (col in 0 until data.columns) {
                        th(classes = CSS_TABLE_VIEW_HEADER) {
                            renderer(
                                this,
                                renderingContext(filterValue, null, Int.MIN_VALUE, col),
                                data.headerCell(col)
                            )
                            onClickFunction = { if (delegate.sorting) onHeaderClick(col) }
                        }
                    }
                }
            }
        }
    }

    private fun TABLE.body() {
        for (row in 0 until data.rows) {
            val classes = if (row % 2 == 0) CSS_TABLE_VIEW_ROW_EVEN else CSS_TABLE_VIEW_ROW_ODD
            tr(classes = classes) {
                for (col in 0 until data.columns) {
                    td {
                        delegate.render.cell(
                            this,
                            renderingContext(filterValue, data.rowItem(row), row, col),
                            data.cell(row, col)
                        )
                        onClickFunction = { ev -> onRowClick(ev.target as HTMLElement, row, col) }
                    }
                }
            }
        }
    }

    private fun TABLE.footer() {
        delegate.render.footer?.let { renderer ->
            tfoot {
                tr {
                    for (col in 0 until data.columns) {
                        th(classes = CSS_TABLE_VIEW_FOOTER) {
                            renderer(
                                this,
                                renderingContext(filterValue, null, Int.MAX_VALUE, col),
                                data.footerCell(col)
                            )
                        }
                    }
                }
            }
        }
    }

    protected open fun renderingContext(filter: String?, item: T?, row: Int, column: Int): IRenderingContext<T> {
        return renderingContext.apply {
            this.filter = filter
            set(item, row, column)
        }
    }

    private fun onRowClick(element: HTMLElement, row: Int, col: Int) {
        if (delegate.selecting) {
            selectedElement?.removeClass(CSS_TABLE_VIEW_ROW_SELECTED)
            element.addClass(CSS_TABLE_VIEW_ROW_SELECTED)
            selectedElement = element
        }
        delegate.cellClickListener?.invoke(data.rowItem(row), row, col)
    }

    companion object {
        const val CSS_TABLE_VIEW = "ui-table-view"
        const val CSS_TABLE_VIEW_FILTER = "ui-table-view-filter"
        const val CSS_TABLE_VIEW_FILTER_INPUT = "ui-table-view-filter-input"
        const val CSS_TABLE_VIEW_HEADER = "ui-table-view-header"
        const val CSS_TABLE_VIEW_FOOTER = "ui-table-view-footer"
        const val CSS_TABLE_VIEW_ROW_ODD = "ui-table-view-row-odd"
        const val CSS_TABLE_VIEW_ROW_EVEN = "ui-table-view-row-even"
        const val CSS_TABLE_VIEW_ROW_SELECTED = "ui-table-view-row-selected"
        const val ID_TABLE_CONTAINER = "ui-table-view-container"
    }
}