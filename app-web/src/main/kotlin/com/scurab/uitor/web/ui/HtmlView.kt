package com.scurab.uitor.web.ui

import com.scurab.uitor.common.util.HasLifecycle
import com.scurab.uitor.common.util.IObservable
import com.scurab.uitor.common.util.Observable
import com.scurab.uitor.common.util.elog
import com.scurab.uitor.common.util.messageSafe
import com.scurab.uitor.web.util.DocumentWrapper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import org.w3c.dom.Element
import org.w3c.dom.HTMLElement
import kotlin.browser.window
import kotlin.coroutines.CoroutineContext
import kotlin.dom.clear

abstract class HtmlView : HasLifecycle, CoroutineScope {
    abstract val element: HTMLElement?

    private val coroutineJob = Job()
    final override val coroutineContext: CoroutineContext = (Dispatchers.Main + coroutineJob)

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
        onDetachObservable.post(this)
        parentElement?.clear()
        onDetached()
        isAttached = false
    }

    open fun onAttached() {
        //let subclass to do something
    }


    open fun onDetached() {
        coroutineJob.cancel()
        //let subclass to do something
    }

    fun alert(e: Throwable) {
        val msg = e.messageSafe
        elog { msg }
        alert(msg)
    }

    fun alert(msg: String) {
        window.alert(msg)
    }

    fun <T> IObservable<T>.observe(observer: (T) -> Unit) {
        this.observe(this@HtmlView, observer)
    }
}