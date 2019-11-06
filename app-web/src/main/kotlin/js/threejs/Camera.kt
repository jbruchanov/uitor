@file:JsQualifier("THREE")

package js.threejs

open external class Camera : Object3D {
    open var matrixWorldInverse: Matrix4
    open var projectionMatrix: Matrix4
    open var projectionMatrixInverse: Matrix4
    open var isCamera: String /* true */
    open fun getWorldDirection(target: Vector3): Vector3
    open fun updateMatrixWorld(force: Boolean? = definedExternally)
}