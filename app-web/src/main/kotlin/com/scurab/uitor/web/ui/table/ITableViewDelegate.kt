package com.scurab.uitor.web.ui.table

/**
 * Configuration for tableview
 */
interface ITableViewDelegate<T> {
    /**
     * Specific Column Renderer
     */
    var render: ITableViewRenderer<T>
    /**
     * Enable/Disable sorting of items via header cell click
     */
    var sorting: Boolean
    /**
     * Enable filtering of elements
     */
    var filtering: Boolean
    /**
     * Pass a value for debounce of filter typing
     */
    var filterDebounce: Int
    /**
     * Explicit html id for the table HtmlElement
     */
    var elementId: String?
    /**
     * Enable selection mode
     */
    var selecting: Boolean

    /**
     * Default cellClickListener
     */
    var cellClickListener: ((item: T, row: Int, column: Int) -> Unit)?
}

/**
 * Default implementation of [ITableViewDelegate] using [TextTableViewRenderer]
 * for cell rendering
 */
open class TableViewDelegate<T>(
    override var render: ITableViewRenderer<T> = TextTableViewRenderer()
) : ITableViewDelegate<T> {
    override var sorting: Boolean = false
    override var filtering: Boolean = false
    override var selecting: Boolean = false
    override var filterDebounce: Int = 200
    override var elementId: String? = null
    override var cellClickListener: ((item: T, row: Int, column: Int) -> Unit)? = null
}