@file:JsQualifier("THREE")
package js.threejs

external interface MeshBasicMaterialParameters : MaterialParameters {
    var map: Texture?
    var color: dynamic /* Color | String | Number */
}

open external class MeshBasicMaterial(parameters: MeshBasicMaterialParameters = definedExternally) :
    Material {
    open var map: Texture?
    open var color: dynamic
    open var wireframe: Boolean
}