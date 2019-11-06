@file:JsQualifier("THREE")
package js.threejs

external interface MorphTarget {
    var name: String
    var vertices: Array<Vector3>
}

external interface MorphColor {
    var name: String
}

external interface MorphNormals {
    var name: String
    var normals: Array<Vector3>
}

external var GeometryIdCount: Number

open external class Geometry : EventDispatcher {
    open fun lookAt(vector: Vector3)
}