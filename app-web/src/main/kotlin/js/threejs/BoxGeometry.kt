@file:JsQualifier("THREE")

package js.threejs

external interface BoxBufferGeometryParams {
    var width: Number
    var height: Number
    var depth: Number
    var widthSegments: Number
    var heightSegments: Number
    var depthSegments: Number
}

open external class BoxBufferGeometry(
    width: Number? = definedExternally,
    height: Number? = definedExternally,
    depth: Number? = definedExternally,
    widthSegments: Number? = definedExternally,
    heightSegments: Number? = definedExternally,
    depthSegments: Number? = definedExternally
) /*: BufferGeometry*/ : Geometry {
    open var parameters: BoxBufferGeometryParams
}

open external class BoxGeometry(
    width: Number? = definedExternally,
    height: Number? = definedExternally,
    depth: Number? = definedExternally,
    widthSegments: Number? = definedExternally,
    heightSegments: Number? = definedExternally,
    depthSegments: Number? = definedExternally
) : Geometry {
    open var parameters: BoxBufferGeometryParams
}