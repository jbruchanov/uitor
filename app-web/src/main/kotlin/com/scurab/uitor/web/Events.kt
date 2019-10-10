package com.scurab.uitor.web

import org.w3c.dom.events.EventTarget
import org.w3c.dom.events.MouseEvent

enum class Events {
    click,
    dblclick,
    mousedown,
    mousemove,
    mouseout,
    mouseover,
    mouseup,
    mousewheel,
    wheel,
}

fun EventTarget.addMouseMoveListener(callback: (MouseEvent) -> Unit) =
    addEventListener(Events.mousemove.name, { event -> callback(event as MouseEvent) })

fun EventTarget.addClickListener(callback: (MouseEvent) -> Unit) =
    addEventListener(Events.click.name, { event -> callback(event as MouseEvent) })