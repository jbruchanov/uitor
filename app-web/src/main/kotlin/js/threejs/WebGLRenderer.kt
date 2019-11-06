@file:JsQualifier("THREE")
package js.threejs

import org.khronos.webgl.WebGLRenderingContext
import org.w3c.dom.HTMLCanvasElement

external interface Renderer {
    var domElement: HTMLCanvasElement
    fun render(scene: Scene, camera: Camera)
    fun setSize(width: Number, height: Number, updateStyle: Boolean? = definedExternally)
}

external interface WebGLRendererParameters {
    var antialias: Boolean?
    var canvas: dynamic /* HTMLCanvasElement | OffscreenCanvas */
    var context: WebGLRenderingContext?
    var precision: String?
    var alpha: Boolean?
    var premultipliedAlpha: Boolean?
    var stencil: Boolean?
    var preserveDrawingBuffer: Boolean?
    var powerPreference: String?
    var depth: Boolean?
    var logarithmicDepthBuffer: Boolean?
}

external interface WebGLDebug {
    var checkShaderErrors: Boolean
}

open external class WebGLRenderer(parameters: WebGLRendererParameters = definedExternally) : Renderer {
    override var domElement: HTMLCanvasElement
    open fun getMaxAnisotropy(): Number
    open fun getPixelRatio(): Number
    open fun setPixelRatio(value: Number)
    open fun getSize(target: Vector2): Vector2
    override fun setSize(width: Number, height: Number, updateStyle: Boolean?)
    override fun render(scene: Scene, camera: Camera)
}