@file:JsQualifier("THREE")
package js.threejs

external interface PerspectiveCameraView {
    var enabled: Boolean
    var fullWidth: Number
    var fullHeight: Number
    var offsetX: Number
    var offsetY: Number
    var width: Number
    var height: Number
}

open external class PerspectiveCamera(
    fov: Number? = definedExternally,
    aspect: Number? = definedExternally,
    near: Number? = definedExternally,
    far: Number? = definedExternally
) : Camera {
    open var isPerspectiveCamera: String /* true */
    open var zoom: Number
    open var fov: Number
    open var aspect: Number
    open var near: Number
    open var far: Number
    open var focus: Number
    open var view: PerspectiveCameraView /* Nothing? | `T$0` */
    open var filmGauge: Number
    open var filmOffset: Number
    open fun setFocalLength(focalLength: Number)
    open fun getFocalLength(): Number
    open fun getEffectiveFOV(): Number
    open fun getFilmWidth(): Number
    open fun getFilmHeight(): Number
    open fun setViewOffset(fullWidth: Number, fullHeight: Number, x: Number, y: Number, width: Number, height: Number)
    open fun clearViewOffset()
    open fun updateProjectionMatrix()
    open fun toJSON(meta: Any? = definedExternally): Any
    open fun setLens(focalLength: Number, frameHeight: Number? = definedExternally)
}