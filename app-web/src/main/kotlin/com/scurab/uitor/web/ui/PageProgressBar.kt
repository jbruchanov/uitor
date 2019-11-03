package com.scurab.uitor.web.ui

import com.scurab.uitor.common.util.dlog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.html.img
import kotlinx.html.js.div
import org.w3c.dom.HTMLElement

private const val CSS_PBAR = "window-progress-bar"
private const val DEFAULT_DELAY = 400L
object PageProgressBar : HtmlView() {
    private val TAG = "PageProgressBar"
    private var job: Job? = null
    override val element: HTMLElement = document.create.div(classes = CSS_PBAR) {
        img(src = "loader.gif")
    }.apply {
        hidden = true
    }

    override fun buildContent() {}

    fun show(delay: Long = 400) {
        dlog(TAG) { "show:delay:$delay" }
        job?.cancel()
        if (delay == 0L) {
            element.hidden = false
        } else {
            job = launch {
                kotlinx.coroutines.delay(delay)
                show(0)
            }
        }
    }

    fun hide() {
        dlog(TAG) { "hide" }
        job?.cancel()
        job = null
        element.hidden = true
    }

    fun withProgressBar(delay: Long = DEFAULT_DELAY, block: () -> Unit) {
        show(delay)
        block()
        hide()
    }
}

fun CoroutineScope.launchWithProgressBar(delay: Long = DEFAULT_DELAY, block: suspend CoroutineScope.() -> Unit): Job {
    PageProgressBar.show(delay)
    val job = launch(block = block)
    job.invokeOnCompletion { PageProgressBar.hide() }
    return job
}