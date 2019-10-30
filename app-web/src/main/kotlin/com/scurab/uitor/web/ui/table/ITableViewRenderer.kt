package com.scurab.uitor.web.ui.table

import kotlinx.html.TD
import kotlinx.html.TH

interface IRenderingContext<T> {
    val filter: String?
    val row: Int
    val column: Int

    operator fun component1(): String? = filter
    operator fun component2(): Int = row
    operator fun component3(): Int = column
}

interface ITableViewRenderer<T> {
    val header: (TH.(IRenderingContext<T>, String?) -> Unit)?
    val cell: (TD.(IRenderingContext<T>, T) -> Unit)
    val footer: (TH.(IRenderingContext<T>, String?) -> Unit)?

}

open class TextTableViewRenderer<T> : ITableViewRenderer<T> {
    override val header: (TH.(IRenderingContext<T>, String?) -> Unit) = { _, item -> text(item ?: "") }
    override val cell: (TD.(IRenderingContext<T>, T) -> Unit) = { _, item -> text(item.toString()) }
    override val footer: (TH.(IRenderingContext<T>, String?) -> Unit) = { _, item -> text(item ?: "") }
}