package com.scurab.uitor.web

import com.scurab.uitor.web.model.ClientConfig
import com.scurab.uitor.web.model.ViewNode
import kotlinx.coroutines.asDeferred
import kotlin.browser.window
import kotlin.js.Json

class ServerApi {

    suspend fun loadViewHierarchy(screenIndex: Int): ViewNode {
        val json = load("viewhierarchy.json")
        return ViewNode(json)
    }

    suspend fun loadClientConfiguration(): ClientConfig {
        val json = load("config.json")
        return ClientConfig(json)
    }

    private suspend fun load(url: String): Json {
        val text = window
            .fetch(url)
            .asDeferred()
            .await()
            .text()
            .asDeferred()
            .await()
        return JSON.parse(text)
    }
}