package com.scurab.uitor.web

import com.scurab.uitor.web.ui.HtmlView
import com.scurab.uitor.web.util.HashToken

abstract class Page : HtmlView() {
    val id: String = this::class.toString().substringAfter("class").trim()

    open fun onHashTokenChanged(old: HashToken, new: HashToken)  {
        //let subclass do something useful
    }
}