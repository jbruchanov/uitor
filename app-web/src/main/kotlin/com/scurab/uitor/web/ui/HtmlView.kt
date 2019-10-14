package com.scurab.uitor.web.ui

import org.w3c.dom.HTMLElement

interface HtmlView {
    val element: HTMLElement
    fun attach(): HtmlView
}