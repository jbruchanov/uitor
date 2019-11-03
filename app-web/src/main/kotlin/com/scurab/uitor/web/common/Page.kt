package com.scurab.uitor.web.common

import com.scurab.uitor.common.util.ref
import com.scurab.uitor.web.ui.HtmlView
import com.scurab.uitor.web.util.HashToken
import com.scurab.uitor.web.util.requireElementById
import org.w3c.dom.Element

abstract class Page : HtmlView() {
    val pageId: String = this::class.toString().substringAfter("class").trim()
    
    open fun onHashTokenChanged(old: HashToken, new: HashToken)  {
        //let subclass do something useful
    }

    abstract fun stateDescription(): String?

    protected fun <T : Element> requireElementById(id: String): T {
        return element.ref.requireElementById(id)
    }
}