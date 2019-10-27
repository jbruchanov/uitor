package com.scurab.uitor.web.ui

import com.scurab.uitor.common.util.Observable
import com.scurab.uitor.common.util.elog
import com.scurab.uitor.web.util.DocumentWrapper
import com.scurab.uitor.web.util.HasLifecycle
import org.w3c.dom.Element
import org.w3c.dom.HTMLElement
import kotlin.browser.window
import kotlin.dom.clear

abstract class HtmlView : HasLifecycle {
    abstract val element: HTMLElement?

    @Suppress("MemberVisibilityCanBePrivate")
    protected var parentElement: Element? = null
    protected var isAttached: Boolean = false; private set
    override val onDetachObservable = Observable<HasLifecycle>()
    protected var document = DocumentWrapper()

    abstract fun buildContent()

    fun attachTo(rootElement: Element) {
        check(!isAttached) { "This view has been already attached" }
        isAttached = true
        parentElement = rootElement
        if (element == null) {
            buildContent()
        }
        onAttachToRoot(rootElement)
        onAttached()
    }

    protected open fun onAttachToRoot(rootElement: Element) {
        element?.let { rootElement.appendChild(it) }
    }

    fun detach() {
        document.dispose()
        onDetachObservable.let {
            it.post(this)
            it.removeObservers()
        }
        parentElement?.clear()
        onDetached()
        isAttached = false
    }

    open fun onAttached() {
        //let subclass to do something
    }


    open fun onDetached() {
        //let subclass to do something
    }

    fun alert(e: Throwable) {
        val msg = e.message ?: "Null Exception"
        elog { msg }
        alert(msg)
    }

    fun alert(msg: String?) {
        window.alert(msg ?: "Null message")
    }
}