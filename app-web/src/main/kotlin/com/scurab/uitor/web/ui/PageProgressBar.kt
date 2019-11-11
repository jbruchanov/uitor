package com.scurab.uitor.web.ui

import com.scurab.uitor.common.util.dlog
import com.scurab.uitor.web.util.DefaultCoroutineErrorHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.html.img
import kotlinx.html.js.div
import kotlinx.html.span
import org.w3c.dom.HTMLElement

private const val CSS_PBAR = "window-progress-bar"
private const val DEFAULT_DELAY = 400L
object PageProgressBar : HtmlView() {
    private val TAG = "PageProgressBar"
    private var job: Job? = null
    private var token = 0
    private var startedTokens = mutableSetOf<Int>()
    private var delayedTokens = mutableSetOf<Int>()
    override val element: HTMLElement = document.create.div(classes = CSS_PBAR) {
        span()
        img(src = "loader.gif")
    }.apply {
        hidden = true
    }

    override fun buildContent() {}

    fun show(delay: Long = 400): Int {
        token++
        return show(delay, token)
    }

    /**
     * Show progress bar
     * @return A token to use with [hide]. Avoid a problem to hide someone else request about the PBar
     */
    private fun show(delay: Long, token: Int): Int {
        dlog(TAG) { "showing delay:$delay, token:${token}, startedTokens:${startedTokens}, delayedTokens:${delayedTokens}" }
        if (delay == 0L) {
            if (delayedTokens.contains(token)) {
                delayedTokens.remove(token)
                startedTokens.add(token)
                element.hidden = false
            }
        } else {
            delayedTokens.add(token)
            job = launch {
                kotlinx.coroutines.delay(delay)
                show(0, token)
            }
        }
        return token
    }

    /**
     * Hide progress bar. Pass a token to be sure you are not going to hide pbar if there was another request
     * [token] Token from [show], pass '-1' to hide no matter other requests.
     */
    fun hide(token: Int/* = -1*/) {
        dlog(TAG) { "hiding token:$token, started:${startedTokens} delayedTokens:${delayedTokens}" }
        if (token == -1) {
            delayedTokens.clear()
            startedTokens.clear()
        }
        delayedTokens.remove(token)
        startedTokens.remove(token)
        if (startedTokens.isEmpty() && delayedTokens.isEmpty()) {
            job?.cancel()
            job = null
            element.hidden = true
        }
    }
}

fun CoroutineScope.launchWithProgressBar(delay: Long = DEFAULT_DELAY, block: suspend CoroutineScope.() -> Unit): Job {
    val token = PageProgressBar.show(delay)
    val job = launch(context = DefaultCoroutineErrorHandler, block = block)
    job.invokeOnCompletion { PageProgressBar.hide(token) }
    return job
}