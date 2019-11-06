@file:JsQualifier("THREE")
package js.threejs

external interface Vector

open external class Vector2(x: Number? = definedExternally, y: Number? = definedExternally) :
    Vector {
    open var x: Number
    open var y: Number
    open var width: Number
    open var height: Number
}