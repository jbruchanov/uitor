package com.scurab.uitor.web.util

import com.scurab.uitor.web.common.Events
import kotlinx.html.dom.create
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.events.Event
import org.w3c.dom.events.KeyboardEvent
import org.w3c.dom.events.MouseEvent
import org.w3c.dom.events.WheelEvent
import kotlin.browser.document
import kotlin.browser.window

private typealias HtmlEventListener = (Event) -> Unit

/**
 * Help class to help track with global listeners and avoid using direct document reference
 */
class DocumentWrapper(document: Document = window.document) {
    private val listeners = mutableMapOf<Events, MutableSet<HtmlEventListener>>()

    fun dispose() {
        listeners.forEach { (event, listener) ->
            listener.forEach {
                when (event) {
                    Events.resize -> window.removeEventListener(event.name, it)
                    else -> document.removeEventListener(event.name, it)
                }
            }
        }
    }

    private fun addEventListener(event: Events, param: HtmlEventListener) {
        val set = listeners.getOrPut(event) { mutableSetOf() }
        set.add(param)
        when (event) {
            Events.resize -> window.addEventListener(event.name, param)
            else -> document.addEventListener(event.name, param)
        }
    }

    fun addMouseLeaveListener(callback: (MouseEvent) -> Unit) {
        addEventListener(Events.mouseleave) { event -> callback(event as MouseEvent) }
    }

    fun addMouseUpListener(callback: (MouseEvent) -> Unit) =
        addEventListener(Events.mouseup) { event -> callback(event as MouseEvent) }

    fun addMouseMoveListener(callback: (MouseEvent) -> Unit) =
        addEventListener(Events.mousemove) { event -> callback(event as MouseEvent) }

    fun addMouseWheelListener(callback: (WheelEvent) -> Unit) =
        addEventListener(Events.mousewheel) { event -> callback(event as WheelEvent) }

    fun addKeyDownListener(callback: (KeyboardEvent) -> Unit) =
        addEventListener(Events.keydown) { event -> callback(event as KeyboardEvent) }

    fun requireElementsByClass(clazz: String): Array<Element> {
        return document.requireElementsByClass(clazz)
    }

    fun addWindowResizeListener(callback: (Event) -> Unit) {
        addEventListener(Events.resize) { event -> callback(event) }
    }

    fun getElementById(id: String): Element? {
        return document.getElementById(id)
    }

    val create = document.create
}