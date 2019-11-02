package com.scurab.uitor.web.common

import com.scurab.uitor.common.util.ise
import com.scurab.uitor.web.model.ClientConfig
import com.scurab.uitor.web.model.ResourceItem
import com.scurab.uitor.web.model.ViewNode
import com.scurab.uitor.web.util.keys
import com.scurab.uitor.web.util.requireTypedListOf
import kotlinx.coroutines.asDeferred
import kotlinx.coroutines.withTimeout
import kotlin.browser.window
import kotlin.js.Json

class ServerApi {

    suspend fun viewHierarchy(screenIndex: Int): ViewNode {
        val json = load("viewhierarchy.json?screenIndex=${screenIndex}")
        return ViewNode(json)
    }

    suspend fun clientConfiguration(): ClientConfig {
        val json = load("config.json")
        return ClientConfig(json)
    }

    suspend fun activeScreens(): Array<String> {
        val json = load("screens.json")
        return json.unsafeCast<Array<String>>()
    }

    suspend fun loadResources(): MutableMap<String, List<Triple<Int, String, String?>>> {
        val result = mutableMapOf<String, List<Triple<Int, String, String?>>>()
        load("resources.json").let { json ->
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
        val json = load("resources.json?id=$resId")
        return ResourceItem(json)
    }

    private suspend fun load(url: String, timeOut: Long = 2000): Json {
        return withTimeout(timeOut) {
            val response = window.fetch(url).asDeferred().await()
            check(response.status == 200.toShort()) { "[${response.status}]${response.statusText}" }
            val text = response
                .text()
                .asDeferred()
                .await()
            JSON.parse<Json>(text)
        }
    }
}