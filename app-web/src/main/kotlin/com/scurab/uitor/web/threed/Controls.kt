@file:JsQualifier("THREE")
@file:Suppress(
    "INTERFACE_WITH_SUPERCLASS",
    "OVERRIDING_FINAL_MEMBER",
    "RETURN_TYPE_MISMATCH_ON_OVERRIDE",
    "CONFLICTING_OVERLOADS",
    "EXTERNAL_DELEGATION"
)

package THREE

import org.w3c.dom.Element

external class TrackballControls(camera: Camera, element: Element) {
    var staticMoving: Boolean
    var dynamicDampingFactor: Double
    var enabled: Boolean
    // How far you can dolly in and out ( PerspectiveCamera only )
    var minDistance: Double
    var maxDistance: Double
    fun addEventListener(name: String, callback: (dynamic) -> Unit)
    fun update()
}