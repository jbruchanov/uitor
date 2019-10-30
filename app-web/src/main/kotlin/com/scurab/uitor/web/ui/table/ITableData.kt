package com.scurab.uitor.web.ui.table

typealias IFilterAction<T> = ((filter: String?, elements: List<Array<T>>) -> List<Array<T>>)

interface ITableData<T> {
    val rows: Int
    val columns: Int
    fun row(row: Int): Row<T>
    fun headerCell(column: Int): String?
    fun cell(row: Int, column: Int): T
    fun footerCell(column: Int): String?
    fun sortedBy(column: Int)
    fun sortedByDescending(column: Int)
    fun filter(key: String?)
}

interface Row<T> {
    operator fun get(column: Int): T
}

class InvalidRow<T> : Row<T> {
    override fun get(column: Int): T {
        throw UnsupportedOperationException("InvalidRowSelection can't be used as row source")
    }
}

class ArrayRow<T>(private val array: Array<T>) : Row<T> {
    override fun get(column: Int): T = array[column]
}

class ListRow<T>(private val list: List<T>) : Row<T> {
    override fun get(column: Int): T = list[column]
}