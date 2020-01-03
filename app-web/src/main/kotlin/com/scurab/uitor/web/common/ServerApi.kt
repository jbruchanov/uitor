package com.scurab.uitor.web.common

import com.scurab.uitor.web.model.ClientConfig
import com.scurab.uitor.web.model.FSItem
import com.scurab.uitor.web.model.IClientConfig
import com.scurab.uitor.web.model.IResourceDTO
import com.scurab.uitor.web.model.Pages
import com.scurab.uitor.web.model.ResourceDTO
import com.scurab.uitor.web.model.ResourceItem
import com.scurab.uitor.web.model.ScreenNode
import com.scurab.uitor.web.model.Snapshot
import com.scurab.uitor.web.model.ViewNode
import com.scurab.uitor.web.model.ViewPropertyItem
import com.scurab.uitor.web.util.Browser
import com.scurab.uitor.web.util.keys
import com.scurab.uitor.web.util.loadImage
import com.scurab.uitor.web.util.obj
import com.scurab.uitor.web.util.requireTypedListOf
import com.scurab.uitor.web.util.toYMHhms
import kotlinx.coroutines.Deferred
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
    suspend fun clientConfiguration(): IClientConfig
    suspend fun activeScreens(): Array<String>
    suspend fun loadResourceItem(): Map<String, List<IResourceDTO>>
    suspend fun loadResourceItem(screenIndex: Int, resId: Int): ResourceItem
    suspend fun loadFileStorage(path: String = ""): List<FSItem>
    suspend fun loadViewProperty(screenIndex: Int, position: Int, property: String): ViewPropertyItem
    suspend fun executeGroovyCode(code: String): String
    suspend fun screenComponents(): ScreenNode
    fun screenShotUrl(screenIndex: Int): String
    fun viewShotUrl(screenIndex: Int, viewIndex: Int): String
    fun logCatUrl(): String
    fun screenStructureUrl(): String

    val supportsViewPropertyDetails : Boolean
}

class ServerApi : IServerApi {

    private suspend fun rawViewHierarchy(screenIndex: Int): Json = loadText("viewhierarchy/$screenIndex").parseJson()
    private suspend fun rawClientConfiguration(): Json = loadText("config").parseJson()
    private suspend fun rawActiveScreens(): Json = loadText("screens").parseJson()
    private suspend fun rawResourceItem(screenIndex: Int, resId: Int): Json = loadText("resources/$screenIndex/$resId").parseJson()
    private suspend fun rawScreenComponents(): Json = loadText("screencomponents").parseJson()

    override suspend fun snapshot(screenIndex: Int) : Snapshot = coroutineScope {
        val taken = Date().toYMHhms()
        val imageTask = async { loadImage(screenShotUrl(screenIndex)) }
        val viewHierarchyTask = async { rawViewHierarchy(screenIndex) }
        val clientConfigTask = async { rawClientConfiguration() }
        val screenComponentsTask = async { rawScreenComponents() }
        val logcatTask = async { loadText(logCatUrl()) }
        val screenStructureTask = async { loadText(screenStructureUrl()) }
        var resourcesTask: Deferred<Json>? = null

        val screenName = activeScreens()[screenIndex]
        val viewHierarchy = viewHierarchyTask.await()
        val clientConfig = clientConfigTask.await().apply {
            this[ClientConfig.DETAIL] = "Snapshot: $taken"
            val snapshotResources = (this[ClientConfig.SNAPSHOT_RESOURCES] as? Boolean) ?: false
            if (snapshotResources) {
                resourcesTask = async { loadText(URL_RESOURCES_ALL).parseJson<Json>() }
            }
            this[ClientConfig.PAGES] = arrayOf(
                Pages.LayoutInspector,
                Pages.ThreeD,
                Pages.TidyTree,
                Pages.Screenshot,
                Pages.Windows,
                Pages.WindowsDetailed,
                Pages.LogCat,
                if (snapshotResources) Pages.Resources else "!${Pages.Resources}"//just keeping it for debug
            )
        }
        val screenshot = imageTask.await()
        val screenStructure = screenStructureTask.await()
        val screenComponents = screenComponentsTask.await()
        val logCat = logcatTask.await()
        val viewShots = ViewNode(viewHierarchy).all()
            .map { vn ->
                if (vn.shouldRender) {
                    loadImage(viewShotUrl(screenIndex, vn.position))
                } else null
            }.toTypedArray()
        val resources = resourcesTask?.await()

        val obj = obj<Snapshot> {
            this.name = screenName
            this.viewHierarchy = viewHierarchy
            this.clientConfiguration = clientConfig
            this.screenshot = screenshot
            this.viewShots = viewShots
            this.screenComponents = screenComponents
            this.logCat = "data:text/plain,$logCat"
            this.screenStructure = "data:${Browser.CONTENT_JSON},$screenStructure"
            this.taken = taken
            this.resources = resources
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
        load<Json>(URL_RESOURCES_LIST).let { json ->
            json.keys().forEach { group ->
                result[group] = json.requireTypedListOf(group) {
                    ResourceDTO.fromJson(it)
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

    override fun viewShotUrl(screenIndex: Int, viewIndex: Int): String = "view/$screenIndex/$viewIndex"

    override fun logCatUrl(): String = "logcat"

    override fun screenStructureUrl(): String = "screenstructure"

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

    suspend fun loadText(url: String, timeOut: Long = 10000): String {
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
        return JSON.parse(loadText(url, timeOut))
    }

    private fun <T> String.parseJson() : T {
        return JSON.parse(this)
    }

    companion object {
        const val URL_RESOURCES_ALL = "resources/all"
        const val URL_RESOURCES_LIST = "resources/list"

        fun storageUrl(path: String = ""): String {
            return "storage?path=$path"
        }

        fun viewPropertyUrl(screenIndex: Int, position: Int, property: String, maxDepth: Int = 0): String {
            return "view/$screenIndex/$position/$property/false/$maxDepth/"
        }
    }
}