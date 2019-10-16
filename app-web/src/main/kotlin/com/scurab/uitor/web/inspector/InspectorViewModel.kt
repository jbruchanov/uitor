package com.scurab.uitor.web.inspector

import com.scurab.uitor.common.util.Observable
import com.scurab.uitor.web.model.ViewNode

class InspectorViewModel {

    val rootNode = Observable<ViewNode?>()
    val hoveredNode = Observable<ViewNode?>()
    val selectedNode = Observable<ViewNode?>()
    val screenIndex = 0
}