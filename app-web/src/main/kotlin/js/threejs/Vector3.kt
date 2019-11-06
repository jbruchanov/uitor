@file:JsQualifier("THREE")
package js.threejs

open external class Vector3(
    x: Number? = definedExternally,
    y: Number? = definedExternally,
    z: Number? = definedExternally
) : Vector {
    open var x: Double
    open var y: Double
    open var z: Double
    open fun set(x: Number, y: Number, z: Number): Vector3 /* this */
}