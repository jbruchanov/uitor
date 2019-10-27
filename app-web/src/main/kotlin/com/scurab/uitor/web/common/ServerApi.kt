package com.scurab.uitor.web.common

import com.scurab.uitor.web.model.ClientConfig
import com.scurab.uitor.web.model.ViewNode
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