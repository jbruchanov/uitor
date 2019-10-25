package com.scurab.uitor.web.ui

import org.w3c.dom.Element
import org.w3c.dom.HTMLElement

abstract class HtmlView {
    abstract val element: HTMLElement

    @Suppress("MemberVisibilityCanBePrivate")
    protected var parentElement: Element? = null
    private var attached: Boolean = false

    fun attachTo(rootElement: Element) {
        check(!attached) { "This view has been already attached" }
        attached = true
        parentElement = rootElement
        buildContent()
        onAttachToRoot(rootElement)
        onAttached()
    }

    protected open fun onAttachToRoot(rootElement: Element) {
        rootElement.appendChild(element)
    }

    abstract fun buildContent()

    open fun onAttached() {
        //TODO let subclass to do something
    }
}