@file:JsQualifier("THREE")
package threejs

external var LineStrip: Number

external var LinePieces: Number

open external class LineSegments : Line {
    constructor(geometry: Geometry?, material: Material?, mode: Number? = definedExternally)
}