package com.scurab.uitor.web.util

import com.scurab.uitor.common.render.Color
import com.scurab.uitor.common.util.npe
import kotlinx.html.Tag
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.HTMLCollection
import org.w3c.dom.HTMLOptionsCollection
import org.w3c.dom.get

const val ATTR_STYLE = "style"

var Tag.styleAttributes: String
    get() = attributes[ATTR_STYLE] ?: ""
    set(value) {
        attributes[ATTR_STYLE] = value
    }

fun Color.styleBackgroundColor(): String = "background-color:${htmlRGBA}"
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

/**
 * Helper method to create javascript iface object used as json args
 */
fun <T> obj(initBlock: T.() -> Unit): T {
    return (js("{}") as T).apply(initBlock)
}

fun <T : Element> Element.getElementById(id: String): T? {
    if (this.id == id) {
        return this as T
    } else {
        for (i in (0 until children.length)) {
            val ch = children[i] ?: continue
            val r: T? = ch.getElementById<T>(id)
            if (r != null) {
                return r
            }
        }
    }
    return null
}

fun <T : Element> Element.requireElementById(id: String): T {
    return getElementById(id) ?: throw NullPointerException("Unable to find element with id:'$id'")
}

fun Element.getElementByClass(clazz: String, to: MutableList<Element> = mutableListOf()): List<Element> {
    if (this.className.split(" ").contains(clazz)) {
        to.add(this)
    }
    for (i in (0 until children.length)) {
        val ch = children[i] ?: continue
        ch.getElementByClass(clazz, to)
    }
    return to
}

fun HTMLOptionsCollection.removeAll() {
    while (this.length > 0) {
        remove(0)
    }
}
