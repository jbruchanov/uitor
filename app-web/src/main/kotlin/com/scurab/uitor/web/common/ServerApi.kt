package com.scurab.uitor.web.common

import com.scurab.uitor.common.util.ise
import com.scurab.uitor.web.model.ClientConfig
import com.scurab.uitor.web.model.FSItem
import com.scurab.uitor.web.model.ResourceItem
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

    suspend fun viewHierarchy(screenIndex: Int): ViewNode {
        val json = load<Json>("viewhierarchy.json?screenIndex=${screenIndex}")
        return ViewNode(json)
    }

    suspend fun clientConfiguration(): ClientConfig {
        val json = load<Json>("config.json")
        return ClientConfig(json)
    }

    suspend fun activeScreens(): Array<String> {
        val json = load<Json>("screens.json")
        return json.unsafeCast<Array<String>>()
    }

    suspend fun loadResources(): MutableMap<String, List<Triple<Int, String, String?>>> {
        val result = mutableMapOf<String, List<Triple<Int, String, String?>>>()
        load<Json>("resources.json").let { json ->
            json.keys().forEach { group ->
                result.put(group, json.requireTypedListOf(group) {
                    val k = it["Key"] as? Int ?: ise("Missing Int field 'Key' in resources response")
                    val v = it["Value"] as? String ?: ise("Missing String field 'Value' in resources response")
                    val l = it["Value1"] as? String
                    Triple(k, v, l)
                })
            }
        }
        return result
    }

    suspend fun loadResources(resId: Int): ResourceItem {
        val json = load<Json>("resources.json?id=$resId")
        return ResourceItem(json)
    }

    suspend fun loadFileStorage(path: String = ""): List<FSItem> {
        val items = load<Array<Json>>("storage.json?path=$path")
        return items.map { FSItem(it) }
    }

    suspend fun loadViewProperty(screenIndex: Int, position: Int, property: String): ViewPropertyItem {
        val json = load<Json>(viewPropertyUrl(screenIndex, position, property))
        return ViewPropertyItem(json)
    }

    fun viewPropertyUrl(screenIndex: Int, position: Int, property: String, maxDepth: Int = 0): String {
        var url = "viewproperty.json?screenIndex=$screenIndex&position=$position&property=$property"
        if (maxDepth > 0) {
            url += "&maxDepth=$maxDepth"
        }
        return url
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

    private suspend fun <T> load(url: String, timeOut: Long = 10000): T {
        return withTimeout(timeOut) {
            val response = window.fetch(url).asDeferred().await()
            check(response.status == 200.toShort()) { "[${response.status}]${response.statusText}" }
            val text = response
                .text()
                .asDeferred()
                .await()
            JSON.parse<T>(text)
        }
    }
}