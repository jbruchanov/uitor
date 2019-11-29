package com.scurab.uitor.web.common

import com.scurab.uitor.common.util.iae
import com.scurab.uitor.web.model.ClientConfig
import com.scurab.uitor.web.model.FSItem
import com.scurab.uitor.web.model.ResourceDTO
import com.scurab.uitor.web.model.ResourceItem
import com.scurab.uitor.web.model.ScreenNode
import com.scurab.uitor.web.model.Snapshot
import com.scurab.uitor.web.model.ViewNode
import com.scurab.uitor.web.model.ViewPropertyItem


class SnapshotServerApi(private val snapshot: Snapshot) : IServerApi {

    override suspend fun snapshot(screenIndex: Int): Snapshot = snapshot

    override val supportsViewPropertyDetails: Boolean = false

    override suspend fun viewHierarchy(screenIndex: Int): ViewNode {
        screenIndex.assertZero()
        return ViewNode(snapshot.viewHierarchy)
    }

    override suspend fun clientConfiguration(): ClientConfig {
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

    override suspend fun loadResourceItem(): MutableMap<String, List<ResourceDTO>> {
        throw UnsupportedOperationException("loadResourceItem()")
    }

    override suspend fun loadResourceItem(screenIndex: Int, resId: Int): ResourceItem {
        throw UnsupportedOperationException("loadResourceItem(screenIndex: Int, resId: Int)")
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