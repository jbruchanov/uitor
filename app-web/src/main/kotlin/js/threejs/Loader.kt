@file:JsQualifier("THREE")
package js.threejs

open external class Loader(manager: LoadingManager? = definedExternally) {
    open var crossOrigin: String
    open var path: String
    open var resourcePath: String
    open var manager: LoadingManager
    open fun setCrossOrigin(crossOrigin: String): Loader /* this */
    open fun setPath(path: String): Loader /* this */
    open fun setResourcePath(resourcePath: String): Loader /* this */
}