package com.scurab.uitor.web.ui.table

open class TableData<T : Comparable<T>> constructor(
    private val headers: Array<String>,
    private val initElements: List<Array<T>>,
    private val footers: Array<String> = emptyArray()
) : ITableData<T> {

    private var elements: List<Array<T>> = initElements
    var filterAction: IFilterAction<T> = stringContainsFilterAction()

    init {
        val sizes = elements.groupBy { it.size }
        check(sizes.size <= 1) { "Different sizes of columns:${sizes.keys}" }
        check(headers.size == sizes.keys.firstOrNull() ?: 0)
        { "Headers count:${headers.size} and items count:${sizes.keys.first()} is different" }
    }

    override val rows: Int get() = elements.size
    override val columns: Int = elements.firstOrNull()?.size ?: 0
    override fun headerCell(column: Int): String? = headers.getOrNull(column)
    override fun cell(row: Int, column: Int): T = elements[row][column]
    override fun footerCell(column: Int): String? = footers.getOrNull(column)
    override fun row(row: Int): Row<T> = ArrayRow(elements[row])

    override fun sortedBy(column: Int) {
        elements = sortingKeys(column).sortedBy { it.first }.sortedResult()
    }

    override fun sortedByDescending(column: Int) {
        elements = sortingKeys(column).sortedByDescending { it.first }.sortedResult()
    }

    override fun filter(key: String?) {
        elements = filterAction(key, initElements)
    }

    private fun sortingKeys(column: Int) = elements.mapIndexed { i, array -> Pair(array[column], i) }
    private fun List<Pair<*, Int>>.sortedResult(): List<Array<T>> = map { (_, i) -> elements[i] }

    companion object {
        fun <T : Comparable<T>> empty(): TableData<T> = TableData(emptyArray(), emptyList())
        fun <T> stringContainsFilterAction(): IFilterAction<T> = { key, rows ->
            key?.let {
                rows.filter { row -> row.any { col -> col.toString().contains(key, true) } }
            } ?: rows
        }
    }
}

