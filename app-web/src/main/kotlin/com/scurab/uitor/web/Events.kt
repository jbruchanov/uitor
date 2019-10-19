package com.scurab.uitor.web

import org.w3c.dom.events.EventTarget
import org.w3c.dom.events.MouseEvent
import org.w3c.dom.events.WheelEvent

enum class Events {
    click,
    dblclick,
    mousedown,
    mousemove,
    mouseout,
    mouseleave,
    mouseover,
    mouseup,
    mousewheel,
    wheel,
    keydown,
}

fun EventTarget.addMouseClickListener(callback: (MouseEvent) -> Unit) =
    addEventListener(Events.click.name, { event -> callback(event as MouseEvent) })

fun EventTarget.addMouseDoubleClickListener(callback: (MouseEvent) -> Unit) =
    addEventListener(Events.dblclick.name, { event -> callback(event as MouseEvent) })

fun EventTarget.addMouseDownListener(callback: (MouseEvent) -> Unit) =
    addEventListener(Events.mousedown.name, { event -> callback(event as MouseEvent) })

fun EventTarget.addMouseUpListener(callback: (MouseEvent) -> Unit) =
    addEventListener(Events.mouseup.name, { event -> callback(event as MouseEvent) })

fun EventTarget.addMouseMoveListener(callback: (MouseEvent) -> Unit) =
    addEventListener(Events.mousemove.name, { event -> callback(event as MouseEvent) })

fun EventTarget.addMouseWheelListener(callback: (WheelEvent) -> Unit) =
    addEventListener(Events.mousewheel.name, { event -> callback(event as WheelEvent) })

fun EventTarget.addMouseOutListener(callback: (MouseEvent) -> Unit) =
    addEventListener(Events.mouseout.name, { event -> callback(event as MouseEvent) })

fun EventTarget.addMouseLeaveListener(callback: (MouseEvent) -> Unit) =
    addEventListener(Events.mouseleave.name, { event -> callback(event as MouseEvent) })

