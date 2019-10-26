@file:JsQualifier("THREE")
package threejs


external interface LineBasicMaterialParameters : MaterialParameters {
    var color: dynamic /* Color | String | Number */
    var linewidth: Number?
    var linecap: String?
    var linejoin: String?
}

open external class LineBasicMaterial(parameters: LineBasicMaterialParameters = definedExternally) :
    Material {
    open var color: Color
    open var linewidth: Number
    open var linecap: String
    open var linejoin: String
    open fun setValues(parameters: LineBasicMaterialParameters)
}