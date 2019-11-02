package com.scurab.uitor.web.ui.table

import com.scurab.uitor.web.model.ClientConfig
import com.scurab.uitor.web.ui.ViewPropertiesTableViewComponents

interface ITableViewDelegate<T> {
    var data: ITableData<T>
    var render: ITableViewRenderer<T>
    val enableSorting: Boolean
    val enableFilter: Boolean
    val filterDebounce: Int
    val elementId: String?
}

open class TableViewDelegate<T>(
    override var data: ITableData<T>,
    override var render: ITableViewRenderer<T> = TextTableViewRenderer()
) : ITableViewDelegate<T> {
    override var enableSorting: Boolean = true
    override var enableFilter: Boolean = true
    override var filterDebounce: Int = 200
    override var elementId: String? = null

    companion object {
        fun defaultViewProperties(clientConfig: ClientConfig) = TableViewDelegate(
            TableData.empty(),
            ViewPropertiesTableViewComponents.columnRenderer(clientConfig)
        )
    }
}