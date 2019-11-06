@file:JsQualifier("THREE")
package js.threejs

external var MaterialIdCount: Number

external interface MaterialParameters {
    var depthWrite: Boolean?
    var name: String?
    var opacity: Number?
    var side: Side?
    var transparent: Boolean?
}

open external class Material : EventDispatcher {
    open var needsUpdate: Boolean
    open var opacity: Number
    open var transparent: Boolean
}