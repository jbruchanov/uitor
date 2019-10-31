package com.scurab.uitor.web.inspector

import com.scurab.uitor.common.util.IObservable
import com.scurab.uitor.common.util.Observable
import com.scurab.uitor.web.model.CommonViewModel
import com.scurab.uitor.web.model.PageViewModel
import com.scurab.uitor.web.model.ViewNode

class InspectorViewModel(
    pageViewModel: PageViewModel,
    override val screenIndex: Int = pageViewModel.screenIndex
) : CommonViewModel by pageViewModel {
    val rootNode: IObservable<ViewNode?> = Observable()
    val hoveredNode: IObservable<ViewNode?> = Observable()
    val selectedNode: IObservable<ViewNode?> = Observable()
    val screenPreviewUrl = "/screen.png?screenIndex=$screenIndex"
    val ignoredViewNodeChanged: IObservable<Pair<ViewNode, Boolean>> = Observable()
    //mix for ignoring by id or by position, todo maybe something better
    val ignoringViewNodeIdsOrPositions: MutableSet<Int> = pageViewModel.clientConfig.pointerIgnoreIds.toMutableSet()

    suspend fun load() {
        rootNode.post(serverApi.viewHierarchy(screenIndex))
    }
}