package com.scurab.uitor.web.util

import com.scurab.uitor.common.util.npe
import com.scurab.uitor.web.common.IServerApi
import com.scurab.uitor.web.model.IClientConfig
import com.scurab.uitor.web.ui.launchWithProgressBar
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.html.dom.create
import kotlinx.html.js.a
import kotlinx.html.js.img
import kotlinx.html.js.pre
import kotlinx.html.style
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.events.Event
import org.w3c.dom.url.URL
import org.w3c.files.Blob
import org.w3c.files.BlobPropertyBag
import kotlin.browser.document
import kotlin.browser.window
import kotlin.js.Date

object Browser {
    const val CONTENT_JSON = "text/json"
    const val ABOUT_BLANK = "about:blank"
    const val TARGET_BLANK = "_blank"
    /**
     * Initiate download data
     */
    fun download(content: Any, fileName: String, contentType: String) {
        val a = document.create.a()
        val file = Blob(arrayOf(content), BlobPropertyBag(contentType))
        a.href = URL.createObjectURL(file)
        a.download = fileName
        a.click()
        a.remove()
    }

    fun openImageInNewTab(url: String) {
        window.open(ABOUT_BLANK, TARGET_BLANK, "")?.let { window ->
            window.onload = {
                val img = document.create.img {
                    style = "-webkit-user-select: none;margin: auto;cursor: zoom-in;"
                    this.src = url
                }
                window.document.let { doc ->
                    doc.body?.let { body ->
                        body.setAttribute("style", "margin: 0px; background: #0e0e0e;")
                        body.append(img)
                    } ?: npe("No body in document ?!")
                }
            }
        }
    }

    fun openTextDataInNewTab(textData: String) {
        window.open(ABOUT_BLANK, TARGET_BLANK, "")?.let { window ->
            window.onload = {
                val el = document.create.pre {
                    style = "word-wrap: break-word; white-space: pre-wrap;"
                    val type = textData.substringBefore(",")
                    val data = textData.substringAfter(",")
                    val text = if (type.contains("json")) {
                        JSON.stringify(JSON.parse(data), null, 2)
                    } else data
                    text(text)
                }
                window.document.body?.append(el) ?: npe("No body in document ?!")
            }
        }
    }

    fun saveButtonHandler(
        clientConfig: IClientConfig,
        serverApi: IServerApi,
        screenIndex: () -> Int
    ): (Event?) -> Unit {
        return { ev ->
            val button = ev?.target as? HTMLButtonElement
            button?.disabled = true
            GlobalScope.launch {
                launchWithProgressBar {
                    try {
                        val obj = serverApi.snapshot(screenIndex(), clientConfig)
                        val filename = "snapshot-${Date().toYMHhms(false, "_")}.json"
                        download(JSON.stringify(obj), filename, Browser.CONTENT_JSON)
                    } catch (e: Exception) {
                        throw e
                    } finally {
                        button?.disabled = false
                    }
                }
            }
        }
    }
}