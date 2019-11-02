package com.scurab.uitor.web.ui.table

import com.scurab.uitor.web.model.ClientConfig
import com.scurab.uitor.web.ui.ViewPropertiesTableViewComponents

interface ITableViewDelegate<T> {
    var data: ITableData<T>
    var render: ITableViewRenderer<T>
    var sorting: Boolean
    var filtering: Boolean
    var filterDebounce: Int
    var elementId: String?
    var selecting: Boolean
}

open class TableViewDelegate<T>(
    override var data: ITableData<T>,
    override var render: ITableViewRenderer<T> = TextTableViewRenderer()
) : ITableViewDelegate<T> {
    override var sorting: Boolean = true
    override var filtering: Boolean = true
    override var selecting: Boolean = false
    override var filterDebounce: Int = 200
    override var elementId: String? = null

    companion object {
        fun defaultViewProperties(clientConfig: ClientConfig) = TableViewDelegate(
            TableData.empty(),
            ViewPropertiesTableViewComponents.columnRenderer(clientConfig)
        )
    }
}