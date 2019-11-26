package com.scurab.uitor.web.common

import com.scurab.uitor.common.util.ise
import com.scurab.uitor.web.model.ClientConfig
import com.scurab.uitor.web.model.FSItem
import com.scurab.uitor.web.model.ResourceDTO
import com.scurab.uitor.web.model.ResourceItem
import com.scurab.uitor.web.model.ScreenNode
import com.scurab.uitor.web.model.Snapshot
import com.scurab.uitor.web.model.ViewNode
import com.scurab.uitor.web.model.ViewPropertyItem
import com.scurab.uitor.web.util.keys
import com.scurab.uitor.web.util.loadImage
import com.scurab.uitor.web.util.obj
import com.scurab.uitor.web.util.requireTypedListOf
import kotlinx.coroutines.asDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withTimeout
import org.w3c.fetch.Headers
import org.w3c.fetch.NO_CACHE
import org.w3c.fetch.RequestCache
import org.w3c.fetch.RequestInit
import kotlin.browser.window
import kotlin.js.Date
import kotlin.js.Json

interface IServerApi {

    suspend fun snapshot(screenIndex: Int): Snapshot
    suspend fun viewHierarchy(screenIndex: Int): ViewNode
    suspend fun clientConfiguration(): ClientConfig
    suspend fun activeScreens(): Array<String>
    suspend fun loadResourceItem(): MutableMap<String, List<ResourceDTO>>
    suspend fun loadResourceItem(screenIndex: Int, resId: Int): ResourceItem
    suspend fun loadFileStorage(path: String = ""): List<FSItem>
    suspend fun loadViewProperty(screenIndex: Int, position: Int, property: String): ViewPropertyItem
    suspend fun executeGroovyCode(code: String): String
    suspend fun screenComponents(): ScreenNode
    fun screenShotUrl(screenIndex: Int): String
    fun viewShotUrl(screenIndex: Int, viewIndex: Int): String
    val supportsViewPropertyDetails : Boolean
}

class ServerApi : IServerApi {

    private suspend fun rawViewHierarchy(screenIndex: Int): Json = loadText("viewhierarchy/$screenIndex").parseJson()
    private suspend fun rawClientConfiguration(): Json = loadText("config").parseJson()
    private suspend fun rawActiveScreens(): Json = loadText("screens").parseJson()
    private suspend fun rawResourceItem(screenIndex: Int, resId: Int): Json = loadText("resources/$screenIndex/$resId").parseJson()
    private suspend fun rawScreenComponents(): Json = loadText("screencomponents").parseJson()

    override suspend fun snapshot(screenIndex: Int) : Snapshot = coroutineScope {
        val imageTask = async { loadImage(screenShotUrl(screenIndex)) }
        val viewHierarchyTask = async { rawViewHierarchy(screenIndex) }
        val clientConfigTask = async { rawClientConfiguration() }

        val screenName = activeScreens()[screenIndex]
        val viewHierarchy = viewHierarchyTask.await()
        val clientConfig = clientConfigTask.await()
        clientConfig["detail"] = "Snapshot: ${Date().toISOString()}"
        val screenshot = imageTask.await()
        val viewShots = ViewNode(viewHierarchy).all()
            .map { vn ->
                if (vn.shouldRender) {
                    loadImage(viewShotUrl(screenIndex, vn.position))
                } else null
            }.toTypedArray()

        val obj = obj<Snapshot> {
            this.name = screenName
            this.viewHierarchy = viewHierarchy
            this.clientConfiguration = clientConfig
            this.screenshot = screenshot
            this.viewShots = viewShots
        }
        obj
    }

    override suspend fun viewHierarchy(screenIndex: Int): ViewNode {
        return ViewNode(rawViewHierarchy(screenIndex))
    }

    override suspend fun clientConfiguration(): ClientConfig {
        return ClientConfig(rawClientConfiguration())
    }

    override suspend fun activeScreens(): Array<String> {
        return rawActiveScreens().unsafeCast<Array<String>>()
    }

    override suspend fun loadResourceItem(): MutableMap<String, List<ResourceDTO>> {
        val result = mutableMapOf<String, List<ResourceDTO>>()
        load<Json>("/resources").let { json ->
            json.keys().forEach { group ->
                result[group] = json.requireTypedListOf(group) {
                    val k = it["Key"] as? Int ?: ise("Missing Int field 'Key' in resources response")
                    val v = it["Value"] as? String ?: ise("Missing String field 'Value' in resources response")
                    val l = it["Value1"] as? String
                    ResourceDTO(k, v, l)
                }
            }
        }
        return result
    }

    override suspend fun loadResourceItem(screenIndex: Int, resId: Int): ResourceItem {
        return ResourceItem(rawResourceItem(screenIndex, resId))
    }

    override suspend fun loadFileStorage(path: String): List<FSItem> {
        val items = load<Array<Json>>(storageUrl(path))
        return items.map { FSItem(it) }
    }

    override suspend fun loadViewProperty(screenIndex: Int, position: Int, property: String): ViewPropertyItem {
        val json = load<Json>(viewPropertyUrl(screenIndex, position, property))
        return ViewPropertyItem(json)
    }

    override suspend fun screenComponents(): ScreenNode {
        return ScreenNode(rawScreenComponents())
    }

    override fun screenShotUrl(screenIndex: Int): String {
        return "screen/$screenIndex"
    }

    override fun viewShotUrl(screenIndex: Int, viewIndex: Int): String {
        return "view/$screenIndex/$viewIndex"
    }

    override val supportsViewPropertyDetails: Boolean = true

    override suspend fun executeGroovyCode(code: String): String {
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

    companion object {
        fun storageUrl(path: String = ""): String {
            return "storage?path=$path"
        }

        fun viewPropertyUrl(screenIndex: Int, position: Int, property: String, maxDepth: Int = 0): String {
            return "view/$screenIndex/$position/$property/false/$maxDepth/"
        }
    }
}