@file:JsQualifier("THREE")
package js.threejs

import org.w3c.dom.events.Event

external interface EventDispatcherEvent {
    var type: String
}

open external class EventDispatcher {
    open fun addEventListener(type: String, listener: (event: Event) -> Unit)
    open fun hasEventListener(type: String, listener: (event: Event) -> Unit): Boolean
    open fun removeEventListener(type: String, listener: (event: Event) -> Unit)
    open fun dispatchEvent(event: EventDispatcherEvent)
}