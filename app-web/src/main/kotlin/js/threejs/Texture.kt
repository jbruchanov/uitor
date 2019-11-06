@file:JsQualifier("THREE")
package js.threejs

import org.w3c.dom.HTMLImageElement

external var TextureIdCount: Number

open external class Texture : EventDispatcher {
    //    constructor(image: HTMLImageElement?)
    constructor(
        image: HTMLImageElement?,
        mapping: dynamic,
        wrapS: dynamic,
        wrapT: dynamic,
        magFilter: dynamic,
        minFilter: dynamic,
        format: dynamic,
        type: dynamic,
        anisotropy: Number?,
        encoding: dynamic
    )

    open var mapping: dynamic//Mapping
    open var wrapS: Wrapping
    open var wrapT: Wrapping
    open var magFilter: dynamic//TextureFilter
    open var minFilter: dynamic//TextureFilter
    open var anisotropy: Number
    open var format: PixelFormat
    open var offset: Vector2
    open var repeat: Vector2
    open var center: Vector2
    open var rotation: Number
    open var generateMipmaps: Boolean
    open var premultiplyAlpha: Boolean
    open var flipY: Boolean
    open var version: Number
    open var needsUpdate: Boolean

    companion object {
        var DEFAULT_IMAGE: Any
        var DEFAULT_MAPPING: Any
    }
}