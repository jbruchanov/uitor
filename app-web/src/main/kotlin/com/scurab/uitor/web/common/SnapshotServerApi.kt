package com.scurab.uitor.web.common

import com.scurab.uitor.common.util.iae
import com.scurab.uitor.common.util.npe
import com.scurab.uitor.web.model.ClientConfig
import com.scurab.uitor.web.model.FSItem
import com.scurab.uitor.web.model.IClientConfig
import com.scurab.uitor.web.model.IResourceDTO
import com.scurab.uitor.web.model.ResourceDTO
import com.scurab.uitor.web.model.ResourceItem
import com.scurab.uitor.web.model.ScreenNode
import com.scurab.uitor.web.model.Snapshot
import com.scurab.uitor.web.model.ViewNode
import com.scurab.uitor.web.model.ViewPropertyItem
import com.scurab.uitor.web.util.getMap
import kotlin.js.Json


class SnapshotServerApi(private val snapshot: Snapshot) : IServerApi {

    override suspend fun snapshot(screenIndex: Int, clientConfig: IClientConfig): Snapshot = snapshot

    override val supportsViewPropertyDetails: Boolean = false

    private val resourcesDtos : Map<String, List<ResourceDTO>>?
    private val resourcesItems : Map<Int, ResourceItem>?

    init {
        val dtosMap = mutableMapOf<String, MutableList<ResourceDTO>>()
        val itemsMap = mutableMapOf<Int, ResourceItem>()

        snapshot.resources?.getMap(null)?.forEach { (group, v) ->
            val items = v as Array<Json>
            items.forEach { resItem ->
                val item = ResourceItem(resItem)
                val dto = ResourceDTO(item.id, item.name, item.source)
                dtosMap.getOrPut(group) { mutableListOf() }.add(dto)
                itemsMap[item.id] = item
            }
        }
        resourcesDtos = dtosMap
        resourcesItems = itemsMap
    }

    override suspend fun viewHierarchy(screenIndex: Int): ViewNode {
        screenIndex.assertZero()
        return ViewNode(snapshot.viewHierarchy)
    }

    override suspend fun clientConfiguration(): IClientConfig {
        return ClientConfig(snapshot.clientConfiguration)
    }

    override suspend fun activeScreens(): Array<String> {
        return arrayOf(snapshot.name)
    }

    override suspend fun screenComponents(): ScreenNode {
        return ScreenNode(snapshot.screenComponents)
    }

    override fun screenShotUrl(screenIndex: Int): String {
        screenIndex.assertZero()
        return snapshot.screenshot
    }

    override fun viewShotUrl(screenIndex: Int, viewIndex: Int): String {
        screenIndex.assertZero()
        return snapshot.viewShots[viewIndex] ?: ""
    }

    override fun logCatUrl(): String = snapshot.logCat

    override fun screenStructureUrl(): String = snapshot.screenStructure

    override suspend fun loadResourceItem(): Map<String, List<IResourceDTO>> {
        return resourcesDtos ?: npe("Unsupported loadResourceItem()")
    }

    override suspend fun loadResourceItem(screenIndex: Int, resId: Int): ResourceItem {
        resourcesItems ?: npe("Unsupported loadResourceItem(screenIndex: Int, resId: Int)")
        return resourcesItems[resId] ?: npe("Missing resource item for id:$resId")
    }

    override suspend fun loadFileStorage(path: String): List<FSItem> {
        throw UnsupportedOperationException("loadFileStorage(path: String)")
    }

    override suspend fun loadViewProperty(screenIndex: Int, position: Int, property: String): ViewPropertyItem {
        throw UnsupportedOperationException("loadViewProperty(screenIndex: Int, position: Int, property: String)")
    }

    override suspend fun executeGroovyCode(code: String): String {
        throw UnsupportedOperationException("executeGroovyCode")
    }

    private fun Int.assertZero() {
        if (this != 0) {
            iae("Invalid value expected:0 was:$this")
        }
    }
}