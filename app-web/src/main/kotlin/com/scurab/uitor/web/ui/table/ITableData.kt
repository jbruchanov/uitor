package com.scurab.uitor.web.ui.table

typealias IFilterAction<T> = ((filter: String?, elements: List<T>) -> List<T>)

/**
 * Interface to represent TableData.
 * Basic implementation via [TableData]
 */
interface ITableData<T> {
    /**
     * Number of rows
     */
    val rows: Int
    /**
     * Number of columns
     */
    val columns: Int

    /**
     * Get an element representing whole row. Usually your 1 piece of data
     */
    fun rowItem(row: Int): T

    /**
     * Get a header string value for particular column
     */
    fun headerCell(column: Int): String?

    /**
     * Get a value to render in particular cell
     * This is then passed to [ITableViewRenderer]
     */
    fun cell(row: Int, column: Int): Any

    /**
     * Get a footer string value for particular column
     */
    fun footerCell(column: Int): String?

    /**
     * Sort data by a column in Asceding order
     */
    fun sortedBy(column: Int)

    /**
     * Sort data by a column in Descending order
     */
    fun sortedByDescending(column: Int)

    /**
     * Filter data
     */
    fun filter(key: String?)
}