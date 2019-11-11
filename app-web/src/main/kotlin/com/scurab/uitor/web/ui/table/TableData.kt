package com.scurab.uitor.web.ui.table

interface ITableDataItem {
    operator fun get(column: Int): Any
    val tableColumns: Int
}

/**
 * Basic implementation of [ITableData]
 */
open class TableData<T : ITableDataItem> constructor(
    private val headers: Array<String>,
    private val initElements: List<T>,
    private val footers: Array<String> = emptyArray(),
    override val columns: Int = headers.size
) : ITableData<T> {
    private var elements: List<T> = initElements
    /**
     * Specific sorting action
     */
    var filterAction: IFilterAction<T> = stringContainsFilterAction()
    /**
     * Convert value field during sorting to String so it's easily comparable
     */
    var sortingMapper: (column: Int, value: Any) -> String = { _, it -> it.toString() }

    override val rows: Int get() = elements.size
    override fun headerCell(column: Int): String? = headers.getOrNull(column)
    override fun footerCell(column: Int): String? = footers.getOrNull(column)
    override fun rowItem(row: Int) = elements[row]
    override fun cell(row: Int, column: Int): Any = elements[row][column]

    override fun sortedBy(column: Int) {
        elements = sortingKeys(column).sortedBy { it.first }.sortedResult()
    }

    override fun sortedByDescending(column: Int) {
        elements = sortingKeys(column).sortedByDescending { it.first }.sortedResult()
    }

    override fun filter(key: String?) {
        elements = filterAction(key, initElements)
    }

    private fun sortingKeys(column: Int): List<Pair<String, Int>> =
        elements.mapIndexed { i, item -> Pair(sortingMapper(column, item[column]), i) }

    private fun List<Pair<*, Int>>.sortedResult(): List<T> = map { (_, i) -> elements[i] }

    companion object {
        fun <T : ITableDataItem> empty() = TableData(emptyArray(), emptyList<T>())
        fun <T : ITableDataItem> stringContainsFilterAction(): IFilterAction<T> = { key, rows ->
            key?.let {
                rows.filter { item -> item.any(it) }
            } ?: rows
        }

        private fun ITableDataItem.any(filter: String): Boolean {
            for (i in (0 until tableColumns)) {
                if (get(i).toString().contains(filter, true)) {
                    return true
                }
            }
            return false
        }
    }
}

