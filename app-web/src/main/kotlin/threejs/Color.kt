@file:JsQualifier("THREE")
package threejs

external interface HSL {
    var h: Number
    var s: Number
    var l: Number
}

open external class Color(r: Number, g: Number, b: Number) {
    constructor(color: String?)
    constructor(color: Number?)
    open var r: Number
    open var g: Number
    open var b: Number
}