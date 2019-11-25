package com.scurab.uitor.web.common

import com.scurab.uitor.common.util.ise
import com.scurab.uitor.web.model.ClientConfig
import com.scurab.uitor.web.model.FSItem
import com.scurab.uitor.web.model.ResourceItem
import com.scurab.uitor.web.model.ScreenNode
import com.scurab.uitor.web.model.ViewNode
import com.scurab.uitor.web.model.ViewPropertyItem
import com.scurab.uitor.web.util.keys
import com.scurab.uitor.web.util.requireTypedListOf
import kotlinx.coroutines.asDeferred
import kotlinx.coroutines.withTimeout
import org.w3c.fetch.Headers
import org.w3c.fetch.NO_CACHE
import org.w3c.fetch.RequestCache
import org.w3c.fetch.RequestInit
import kotlin.browser.window
import kotlin.js.Json

class ServerApi {

    suspend fun rawViewHierarchy(screenIndex: Int): Json = loadText("viewhierarchy/$screenIndex").parseJson()
    suspend fun rawClientConfiguration(): Json = loadText("config").parseJson()
    suspend fun rawActiveScreens(): Json = loadText("screens").parseJson()

    suspend fun viewHierarchy(screenIndex: Int): ViewNode {
        return ViewNode(rawViewHierarchy(screenIndex))
    }

    suspend fun clientConfiguration(): ClientConfig {
        return ClientConfig(rawClientConfiguration())
    }

    suspend fun activeScreens(): Array<String> {
        return rawActiveScreens().unsafeCast<Array<String>>()
    }

    suspend fun loadResources(): MutableMap<String, List<Triple<Int, String, String?>>> {
        val result = mutableMapOf<String, List<Triple<Int, String, String?>>>()
        load<Json>("/resources").let { json ->
            json.keys().forEach { group ->
                result[group] = json.requireTypedListOf(group) {
                    val k = it["Key"] as? Int ?: ise("Missing Int field 'Key' in resources response")
                    val v = it["Value"] as? String ?: ise("Missing String field 'Value' in resources response")
                    val l = it["Value1"] as? String
                    Triple(k, v, l)
                }
            }
        }
        return result
    }

    suspend fun loadResources(screenIndex: Int, resId: Int): ResourceItem {
        val json = load<Json>("/resources/$screenIndex/$resId")
        return ResourceItem(json)
    }

    suspend fun loadFileStorage(path: String = ""): List<FSItem> {
        val items = load<Array<Json>>(storageUrl(path))
        return items.map { FSItem(it) }
    }

    suspend fun loadViewProperty(screenIndex: Int, position: Int, property: String): ViewPropertyItem {
        val json = load<Json>(viewPropertyUrl(screenIndex, position, property))
        return ViewPropertyItem(json)
    }

    fun viewPropertyUrl(screenIndex: Int, position: Int, property: String, maxDepth: Int = 0): String {
        return "view/$screenIndex/$position/$property/false/$maxDepth/"
    }

    suspend fun executeGroovyCode(code: String): String {
        return withTimeout(15000) {
            val response = window.fetch(
                "groovy", RequestInit(
                    method = "POST",
                    cache = RequestCache.NO_CACHE,
                    headers = Headers().apply {
                        append("Content-Length", code.length.toString())
                    },
                    body = code
                )
            ).asDeferred().await()
            check(response.status == 200.toShort()) { "[${response.status}]${response.statusText}" }
            response.text().asDeferred().await()
        }
    }

    private suspend fun loadText(url: String, timeOut: Long = 10000): String {
        return withTimeout(timeOut) {
            val response = window.fetch(url).asDeferred().await()
            check(response.status == 200.toShort()) { "[${response.status}]${response.statusText}" }
            val text = response
                .text()
                .asDeferred()
                .await()
            text
        }
    }

    private suspend fun <T> load(url: String, timeOut: Long = 10000): T {
        return JSON.parse<T>(loadText(url, timeOut))
    }

    private fun <T> String.parseJson() : T {
        return JSON.parse(this)
    }

    suspend fun screenComponents(): ScreenNode {
        val json = load<Json>("screencomponents")
        return ScreenNode(json)
    }

    companion object {
        fun storageUrl(path: String = ""): String {
            return "storage?path=$path"
        }

        fun screenShotUrl(screenIndex: Int): String {
            return "screen/$screenIndex"
        }

        fun viewShotUrl(screenIndex: Int, viewIndex: Int): String {
            return "view/$screenIndex/$viewIndex"
        }
    }
}