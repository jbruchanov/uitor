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
    override val element: HTMLElement = document.create.div(classes = CSS_PBAR) {
        span()
        img(src = "loader.gif")
    }.apply {
        hidden = true
    }

    override fun buildContent() {}

    fun show(delay: Long = 400): Int = show(delay, true)

    /**
     * Show progress bar
     * @return A token to use with [hide]. Avoid a problem to hide someone else request about the PBar
     */
    private fun show(delay: Long, incCounter: Boolean): Int {
        dlog(TAG) { "show:delay:$delay" }
        job?.cancel()
        job = null
        if (delay == 0L) {
            element.hidden = false
        } else {
            job = launch {
                kotlinx.coroutines.delay(delay)
                show(0, false)
            }
        }
        if (incCounter) {
            token++
        }
        return token
    }

    /**
     * Hide progress bar. Pass a token to be sure you are not going to hide pbar if there was another request
     * [token] Token from [show], pass '-1' to hide no matter other requests.
     */
    fun hide(token: Int/* = -1*/) {
        dlog(TAG) { "hide sameToken:${token == this.token}" }
        if (token == -1) {
            //inc token if hiding explicitly to avoid any potential confusion
            this.token++
        }
        if (token == -1 || token == this.token) {
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