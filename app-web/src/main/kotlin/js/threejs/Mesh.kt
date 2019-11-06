@file:JsQualifier("THREE")
package js.threejs

open external class Mesh : Object3D {
    constructor(geometry: Geometry?, material: Material?)
    constructor(geometry: Geometry?, material: Array<out Material?>?)
    open var material: Material
}