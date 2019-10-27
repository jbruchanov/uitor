package com.scurab.uitor.web.inspector

import com.scurab.uitor.common.util.Observable
import com.scurab.uitor.web.model.CommonViewModel
import com.scurab.uitor.web.model.PageViewModel
import com.scurab.uitor.web.model.ViewNode

class InspectorViewModel(
    pageViewModel: PageViewModel,
    override val screenIndex: Int = pageViewModel.screenIndex
) : CommonViewModel by pageViewModel {
    val rootNode = Observable<ViewNode?>()
    val hoveredNode = Observable<ViewNode?>()
    val selectedNode = Observable<ViewNode?>()
    val screenPreviewUrl = "/screen.png?screenIndex=$screenIndex"

    suspend fun load() {
        rootNode.post(serverApi.viewHierarchy(screenIndex))
    }
}