package com.scurab.uitor.web.ui.table

sealed class Sorting(internal val column: Int) {
    class Ascending(column: Int) : Sorting(column) {
        override fun sort(data: ITableData<*>) {
            data.sortedByDescending(column)
        }
    }

    class Descending(column: Int) : Sorting(column) {
        override fun sort(data: ITableData<*>) {
            data.sortedBy(column)
        }
    }

    abstract fun sort(data: ITableData<*>)

    companion object {
        fun toggleSort(column: Int, knownSorted: Sorting?): Sorting {
            return when {
                column == knownSorted?.column && knownSorted is Ascending -> Descending(column)
                column == knownSorted?.column && knownSorted is Descending -> Ascending(column)
                else -> Ascending(column)
            }
        }
    }
}