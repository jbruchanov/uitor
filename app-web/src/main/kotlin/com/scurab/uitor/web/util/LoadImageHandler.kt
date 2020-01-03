package com.scurab.uitor.web.util

import com.scurab.uitor.common.util.dlog
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import org.w3c.dom.Image
import org.w3c.dom.events.Event
import org.w3c.dom.events.EventListener
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Class to help with loading images into Image using coroutines
 */
private const val ERROR = "error"
private const val LOAD = "load"

internal class LoadImageHandler(
    private val element: Image
) {
    private val TAG = "LoadImageHandler@" + hashCode().toString(16).toUpperCase()
    private var url: String? = null
    private var continuation: CancellableContinuation<Image>? = null

    suspend fun load(url: String): Image {
        return suspendCancellableCoroutine { continuation ->
            this.continuation = continuation
            loadAsync(url)
        }
    }

    fun cancel() {
        dlog(TAG) { "cancel" }
        continuation?.cancel()
    }

    private fun loadAsync(url: String) {
        this.url = url
        dlog(TAG) { "loadAsync:$url" }
        element.addEventListener(LOAD, eventListener)
        element.addEventListener(ERROR, eventListener)
        element.src = url
    }

    private val eventListener = object : EventListener {
        override fun handleEvent(event: Event) {
            detach()
            dlog(TAG) { "handleEvent:${event.type}" }
            when (event.type) {
                ERROR -> continuation?.resumeWithException(IllegalStateException("Unable to load url:'$url'"))
                else -> continuation?.resume(element)
            }
        }

        private fun detach() {
            dlog(TAG) { "detach" }
            element.removeEventListener(LOAD, this)
            element.removeEventListener(ERROR, this)
        }
    }
}