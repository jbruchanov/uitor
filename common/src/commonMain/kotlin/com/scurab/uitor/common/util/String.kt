@file:Suppress("SpellCheckingInspection", "NAME_SHADOWING")

package com.scurab.uitor.common.util

private val EMPTY = IntArray(0)

fun String.isMatchingIndexes(expr: String, ignoreCase: Boolean = true): Boolean {
    val thiz = if (ignoreCase) toLowerCase() else this
    val expr = if (ignoreCase) expr.toLowerCase() else expr
    return expr
        .let { expr.trim() }
        .takeIf { it.isNotEmpty() }
        ?.let { se ->
            var minIndex = -1
            se.forEachIndexed { _, c ->
                val mi = thiz.indexOf(c, minIndex + 1)
                if (mi > minIndex) {
                    minIndex = mi
                } else {
                    return false
                }
            }
            true
        } ?: false
}

/**
 * Return matching indexes.
 * Example:
 * 'HelloWorld'
 * - param 'eoo' will return [1, 4, 6]
 * - param 'HWq' will return [] as the 'q' is not found
 *
 */
fun String.matchingIndexes(expr: String, ignoreCase: Boolean = true): IntArray {
    val thiz = if (ignoreCase) toLowerCase() else this
    val expr = if (ignoreCase) expr.toLowerCase() else expr
    return expr
        .let { expr.trim() }
        .takeIf { it.isNotEmpty() }
        ?.let { se ->
            val result = IntArray(se.length)
            var minIndex = -1
            se.forEachIndexed { index, c ->
                val mi = thiz.indexOf(c, minIndex + 1)
                if (mi > minIndex) {
                    result[index] = mi
                    minIndex = mi
                } else {
                    return EMPTY
                }
            }
            result
        } ?: EMPTY
}

fun String.highlightAt(array: IntArray, hStart: String, hEnd: String = hStart): String {
    if (array.isEmpty()) {
        return this
    }
    val sb = StringBuilder()
    var hIndex = 0
    forEachIndexed { index, c ->
        if (hIndex < array.size && index == array[hIndex]) {
            sb.append(hStart).append(c).append(hEnd)
            hIndex++
        } else {
            sb.append(c)
        }
    }
    return sb.toString()
}

fun String.highlightAt(substring: String, hStart: String, hEnd: String = hStart): String {
    if (substring.isEmpty() || substring.length > this.length) {
        return this
    }

    val sb = StringBuilder()
    var startIndex = this.indexOf(substring, 0, true)
    var i = 0
    while (i < length) {
        if (startIndex == i) {
            sb.append(hStart)
            //add rest of substring matching same case as original
            (substring.indices).forEach { _ ->
                sb.append(this[i++])
            }
            sb.append(hEnd)
            startIndex = this.indexOf(substring, i, true)
        }
        //additional check if internal loops add i++, so it might be already last item
        if (i < length) {
            sb.append(this[i])
        }
        i++
    }
    return sb.toString()
}

fun Char.isUppercase() = this in ('A'..'Z')
fun Char.isLowercase() = this in ('a'..'z')