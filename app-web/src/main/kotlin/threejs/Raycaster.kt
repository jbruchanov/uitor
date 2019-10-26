@file:JsQualifier("THREE")
package threejs

external interface Intersection {
    var distance: Number
    var distanceToRay: Number?
    var point: Vector3
    var index: Number?
    var faceIndex: Number?
    var `object`: Object3D
    @JsName("object")
    var item: Object3D
    var uv: Vector2?
}

external interface RaycasterParameters {
    var Mesh: Any?
    var Line: Any?
    var LOD: Any?
    var Sprite: Any?
}

open external class Raycaster(
    origin: Vector3? = definedExternally,
    direction: Vector3? = definedExternally,
    near: Number? = definedExternally,
    far: Number? = definedExternally
) {
    open var ray: Ray
    open var near: Number
    open var far: Number
    open var camera: Camera
    open var params: RaycasterParameters
    open var linePrecision: Number
    open fun set(origin: Vector3, direction: Vector3)
    open fun setFromCamera(coords: Vector2, camera: Camera)
    open fun intersectObject(
        `object`: Object3D,
        recursive: Boolean? = definedExternally,
        optionalTarget: Array<Intersection>? = definedExternally
    ): Array<Intersection>

    open fun intersectObjects(
        objects: Array<Object3D>,
        recursive: Boolean? = definedExternally,
        optionalTarget: Array<Intersection>? = definedExternally
    ): Array<Intersection>
}