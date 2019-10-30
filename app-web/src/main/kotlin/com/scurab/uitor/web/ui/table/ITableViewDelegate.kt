package com.scurab.uitor.web.ui.table

interface ITableViewDelegate<T> {
    var data: ITableData<T>
    var render: ITableViewRenderer<T>
    val enableFilter: Boolean
    val filterDebounce: Int
}

open class TableViewDelegate<T>(
    override var data: ITableData<T>,
    override var render: ITableViewRenderer<T>,
    override val enableFilter: Boolean = true,
    override val filterDebounce: Int = 200
) : ITableViewDelegate<T>