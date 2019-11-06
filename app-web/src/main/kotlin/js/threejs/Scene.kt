@file:JsQualifier("THREE")
package js.threejs

open external class Scene : Object3D {
    open var autoUpdate: Boolean
    open var background: dynamic /* Nothing? | Color | Texture */
    open fun toJSON(meta: Any? = definedExternally): Any
    open fun dispose()
}