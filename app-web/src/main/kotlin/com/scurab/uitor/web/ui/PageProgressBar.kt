package com.scurab.uitor.web.ui

import com.scurab.uitor.common.util.dlog
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
    private var counter = 0
    override val element: HTMLElement = document.create.div(classes = CSS_PBAR) {
        span()
        img(src = "loader.gif")
    }.apply {
        hidden = true
    }

    override fun buildContent() {}

    fun show(delay: Long = 400): Int = show(delay, true)

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
            counter++
        }
        return counter
    }

    fun hide(counter: Int/* = -1*/) {
        dlog(TAG) { "hide sameToken:${counter == this.counter}" }
        if (counter == -1 || counter == this.counter) {
            job?.cancel()
            job = null
            element.hidden = true
        }
    }
}

fun CoroutineScope.launchWithProgressBar(delay: Long = DEFAULT_DELAY, block: suspend CoroutineScope.() -> Unit): Job {
    val token = PageProgressBar.show(delay)
    val job = launch(block = block)
    job.invokeOnCompletion { PageProgressBar.hide(token) }
    return job
}