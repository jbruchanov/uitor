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
import kotlin.dom.clear

private class RenderingContext<V>(
    override var column: Int = 0,
    override var row: Int = 0,
    override var filter: String? = null
) : IRenderingContext<V> {
    fun set(row: Int, column: Int): RenderingContext<V> {
        this.row = row
        this.column = column
        return this
    }
}

open class TableView<V>(private val delegate: ITableViewDelegate<V>) : HtmlView() {
    override val element: HTMLElement? get() = _element
    protected val tableViewContainer by lazyLifecycled { _element.ref.requireElementById<HTMLElement>(ID_TABLE_CONTAINER) }
    protected var sorting: Sorted? = null; private set
    protected var filterValue: String? = null; private set

    private var _element: HTMLElement? = null
    private var filterChannel = ConflatedBroadcastChannel("")
    private val invalidRow = InvalidRow<V>()
    private val renderingContext = RenderingContext<V>()

    override fun buildContent() {
        _element = document.create.div {
            div {
                if (delegate.enableFilter) {
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
                delegate.data.filter(it)
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
        sorting = Sorted.toggleSort(column, sorting).apply {
            sort(delegate.data)
        }
        refreshContent()
    }

    protected open fun onFilterKeyUp(filter: String) {
        filterChannel.offer(filter.trim())
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
                    for (col in 0 until delegate.data.columns) {
                        th(classes = CSS_TABLE_VIEW_HEADER) {
                            renderer(
                                this,
                                renderingContext(filterValue, Int.MIN_VALUE, col),
                                delegate.data.headerCell(col)
                            )
                            onClickFunction = { if (delegate.enableSorting) onHeaderClick(col) }
                        }
                    }
                }
            }
        }
    }

    private fun TABLE.body() {
        for (row in 0 until delegate.data.rows) {
            val classes = if (row % 2 == 0) CSS_TABLE_VIEW_ROW_EVEN else CSS_TABLE_VIEW_ROW_ODD
            tr(classes = classes) {
                for (col in 0 until delegate.data.columns) {
                    td {
                        delegate.render.cell(
                            this,
                            renderingContext(filterValue, row, col),
                            delegate.data.cell(row, col)
                        )
                    }
                }
            }
        }
    }

    private fun TABLE.footer() {
        delegate.render.footer?.let { renderer ->
            tfoot {
                tr {
                    for (col in 0 until delegate.data.columns) {
                        th(classes = CSS_TABLE_VIEW_FOOTER) {
                            renderer(
                                this,
                                renderingContext(filterValue, Int.MAX_VALUE, col),
                                delegate.data.footerCell(col)
                            )
                        }
                    }
                }
            }
        }
    }

    protected open fun renderingContext(filter: String?, row: Int, column: Int): IRenderingContext<V> {
        return renderingContext.apply {
            this.filter = filter
            set(row, column)
        }
    }

    companion object {
        const val CSS_TABLE_VIEW = "ui-table-view"
        const val CSS_TABLE_VIEW_FILTER = "ui-table-view-filter"
        const val CSS_TABLE_VIEW_FILTER_INPUT = "ui-table-view-filter-input"
        const val CSS_TABLE_VIEW_HEADER = "ui-table-view-header"
        const val CSS_TABLE_VIEW_FOOTER = "ui-table-view-footer"
        const val CSS_TABLE_VIEW_ROW_ODD = "ui-table-view-row-odd"
        const val CSS_TABLE_VIEW_ROW_EVEN = "ui-table-view-row-even"
        const val ID_TABLE_CONTAINER = "ui-table-view-container"
    }
}

sealed class Sorted(internal val column: Int) {
    class Ascending(column: Int) : Sorted(column) {
        override fun sort(data: ITableData<*>) {
            data.sortedByDescending(column)
        }
    }

    class Descending(column: Int) : Sorted(column) {
        override fun sort(data: ITableData<*>) {
            data.sortedBy(column)
        }
    }

    abstract fun sort(data: ITableData<*>)

    companion object {
        fun toggleSort(column: Int, knownSorted: Sorted?): Sorted {
            return when {
                column == knownSorted?.column && knownSorted is Ascending -> Descending(column)
                column == knownSorted?.column && knownSorted is Descending -> Ascending(column)
                else -> Ascending(column)
            }
        }
    }
}