@file:JsQualifier("THREE")
package threejs

external var Object3DIdCount: Number

open external class Object3D : EventDispatcher {
    open var id: Number
    open var uuid: String
    open var name: String
    open var type: String
    open var parent: Object3D?
    open var children: Array<Object3D>
    open var up: Vector3
    open var position: Vector3
    open var rotation: Euler
    open var scale: Vector3
    open var visible: Boolean
    open var userData: Any?
    open fun rotateX(angle: Number): Object3D /* this */

    open fun rotateY(angle: Number): Object3D /* this */
    open fun rotateZ(angle: Number): Object3D /* this */
    open fun translateOnAxis(axis: Vector3, distance: Number): Object3D /* this */
    open fun translateX(distance: Number): Object3D /* this */
    open fun translateY(distance: Number): Object3D /* this */
    open fun translateZ(distance: Number): Object3D /* this */
    open fun lookAt(
        vector: Vector3,
        y: Number? = definedExternally,
        z: Number? = definedExternally
    )

    open fun lookAt(
        vector: Number,
        y: Number? = definedExternally,
        z: Number? = definedExternally
    )

    open fun add(vararg `object`: Object3D): Object3D /* this */
    open fun remove(vararg `object`: Object3D): Object3D /* this */
    open fun attach(`object`: Object3D): Object3D /* this */
    open fun getObjectById(id: Number): Object3D?
    open fun getObjectByName(name: String): Object3D?
    open fun getObjectByProperty(name: String, value: String): Object3D?
}