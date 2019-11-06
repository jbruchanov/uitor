@file:JsQualifier("THREE")
package js.threejs

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
    fun dispose()
}
