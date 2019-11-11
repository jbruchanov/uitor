package com.scurab.uitor.web.util

import com.scurab.uitor.common.util.dlog
import com.scurab.uitor.common.util.elog
import com.scurab.uitor.common.util.messageSafe
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlin.browser.window
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

private const val TAG = "DefaultCoroutineErrorHandler"

open class CustomCoroutineErrorHandler(private val block: (Throwable) -> Unit) :
    AbstractCoroutineContextElement(CoroutineExceptionHandler),
    CoroutineExceptionHandler {

    override fun handleException(context: CoroutineContext, exception: Throwable) {
        when (exception) {
            is CancellationException -> dlog(TAG) { "Coroutine cancelled" }
            else -> {
                block(exception)
            }
        }
    }
}

fun customCoroutineErrorHandler(block: (Throwable) -> Unit): CoroutineExceptionHandler {
    return CustomCoroutineErrorHandler(block)
}

object DefaultCoroutineErrorHandler : CustomCoroutineErrorHandler({
    val msg = it.messageSafe
    elog(TAG) { msg }
    window.alert(msg)
})