@file:JsQualifier("ace")

package js.ace

external fun edit(id: String): Editor

external interface Editor {
    fun getValue(): String
    fun setTheme(name: String)
    val session: Session
}

external interface Session {
    fun setMode(mode: String)
}

