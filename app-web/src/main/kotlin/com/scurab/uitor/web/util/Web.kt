package com.scurab.uitor.web.util

import com.scurab.uitor.common.util.npe
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.HTMLCollection
import org.w3c.dom.get

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