package com.scurab.uitor.web.util

import com.scurab.uitor.common.util.npe
import org.w3c.dom.*

fun Document.requireElementById(id: String) = getElementById(id) ?: npe(id)
fun Document.requireElementsByClass(clazz: String) = getElementsByClassName(clazz).toArray()
fun HTMLCollection.toArray() = Array(this.length) { this[it] ?: npe("Null element at index:$it") }
fun HTMLCollection.indexOf(any: Element): Int {
    for (i in (0 until this.length)) if (this[i] == any) return i
    return -1
}

fun HTMLCollection.forEach(block: (Element) -> Unit) {
    for (i in (0 until this.length)) block(this[i] ?: npe("Null element at index:$i"))
}

fun HTMLCollection.forEachIndexed(block: (Element, Int) -> Unit) {
    for (i in (0 until this.length)) block(this[i] ?: npe("Null element at index:$i"), i)
}

interface IScrollIntoViewArgs {
    val behavior: String
    val block: String
    val inline: String
}

fun scrollIntoViewArgs(
    behavior: String = "smooth",
    verticalAlignment: String = "nearest",
    horizontalAlignment: String = "nearest"
): IScrollIntoViewArgs {
    return object : IScrollIntoViewArgs {
        override val behavior: String = behavior
        override val block: String = verticalAlignment
        override val inline: String = horizontalAlignment
    }
}