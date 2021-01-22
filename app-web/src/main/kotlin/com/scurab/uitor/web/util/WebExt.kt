package com.scurab.uitor.web.util

import com.scurab.uitor.common.render.Color
import com.scurab.uitor.common.util.npe
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.html.Tag
import kotlinx.html.dom.create
import kotlinx.html.js.canvas
import kotlinx.html.js.img
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.HTMLCollection
import org.w3c.dom.HTMLOptionsCollection
import org.w3c.dom.get
import org.w3c.files.Blob
import org.w3c.files.FileReader
import kotlinx.browser.document
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.js.Date


const val ATTR_STYLE = "style"
const val SCROLL_BAR_WIDTH = 17

var Tag.styleAttributes: String
    get() = attributes[ATTR_STYLE] ?: ""
    set(value) {
        attributes[ATTR_STYLE] = value
    }


fun Date.toYMHhms(useSeparators: Boolean = true, dateTimeSeparator: String = " "): String {
    val d = fun Int.(): String { return toString().padStart(2, '0') }
    val dateSep = if (useSeparators) "-" else ""
    val timeSep = if (useSeparators) ":" else ""
    val date = getFullYear().d() + dateSep + (getMonth() + 1).d() + dateSep + getDate().d()
    val time = getHours().d() + timeSep + getMinutes().d() + timeSep + getSeconds().d()
    return date + dateTimeSeparator + time
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
fun <T> obj(initBlock: (T.() -> Unit)? = null): T {
    return (js("{}") as T).apply {
        if (initBlock != null) {
            this.apply(initBlock)
        }
    }
}

fun <T : Element> Element.getElementById(id: String): T? {
    if (this.id == id) {
        return this as T
    } else {
        for (i in (0 until children.length)) {
            val ch = children[i] ?: continue
            val r: T? = ch.getElementById(id)
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

/**
 * Load image from url as the Base64 string
 */
suspend fun loadImage(url:String) : String {
    val img = document.create.img()
    img.crossOrigin = "Anonymous";
    return suspendCancellableCoroutine { continuation ->
        img.onload = {
            try {
                val canvas = document.create.canvas(null, "")
                val ctx = canvas.getContext("2d") as CanvasRenderingContext2D
                canvas.width = img.width
                canvas.height = img.height
                ctx.drawImage(img, 0.0, 0.0)
                val base64 = canvas.toDataURL()
                continuation.resume(base64)
                canvas.remove()
            } catch (e: Exception) {
                continuation.resumeWithException(e)
            }
        }
        img.onerror = { _, _, _, _, _ ->
            continuation.resumeWithException(Exception("Unable to load image url:'$url'"))
        }
        img.src = url
    }
}

suspend fun Blob.readAsText(): String {
    return suspendCancellableCoroutine { coroutine ->
        FileReader().apply {
            onload = {
                coroutine.resume(result as String)
            }
            onerror = {
                coroutine.resumeWithException(Exception("Unable to load data"))
            }
            this.readAsText(this@readAsText)
        }
    }
}