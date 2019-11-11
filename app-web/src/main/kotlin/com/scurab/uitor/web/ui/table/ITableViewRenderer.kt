package com.scurab.uitor.web.ui.table

import kotlinx.html.TD
import kotlinx.html.TH

/**
 * Special context to get more data during rendering of a cell
 */
interface IRenderingContext<T> {
    val item: T?
    val filter: String?
    val row: Int
    val column: Int

    operator fun component1(): T? = item
    operator fun component2(): String? = filter
    operator fun component3(): Int = row
    operator fun component4(): Int = column
}

open class RenderingContext<T : ITableDataItem>(
    override var column: Int = 0,
    override var row: Int = 0,
    override var filter: String? = null
) : IRenderingContext<T> {
    override var item: T? = null
    fun set(item: T?, row: Int, column: Int): RenderingContext<T> {
        this.item = item
        this.row = row
        this.column = column
        return this
    }
}

/**
 * Interface for TableView rendering
 */
interface ITableViewRenderer<T> {
    val header: (TH.(IRenderingContext<T>, String?) -> Unit)?
    val cell: (TD.(IRenderingContext<T>, Any) -> Unit)
    val footer: (TH.(IRenderingContext<T>, String?) -> Unit)?

}

/**
 * Default implementation of Rendering, everything is just simple string
 */
open class TextTableViewRenderer<T> : ITableViewRenderer<T> {
    override val header: (TH.(IRenderingContext<T>, String?) -> Unit)? = { _, item -> text(item ?: "") }
    override val cell: (TD.(IRenderingContext<T>, Any) -> Unit) = { _, item -> text(item.toString()) }
    override var footer: (TH.(IRenderingContext<T>, String?) -> Unit)? = { _, item -> text(item ?: "") }
}