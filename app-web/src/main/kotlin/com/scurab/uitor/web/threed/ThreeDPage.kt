package com.scurab.uitor.web.threed

import com.scurab.uitor.common.util.ref
import com.scurab.uitor.web.common.InspectorPage
import com.scurab.uitor.web.inspector.InspectorViewModel
import com.scurab.uitor.web.model.PageViewModel
import kotlinx.html.js.div
import org.w3c.dom.Element
import org.w3c.dom.HTMLElement

class ThreeDPage(pageViewModel: PageViewModel) : InspectorPage(InspectorViewModel(pageViewModel)) {
    override var element: HTMLElement? = null; private set
    private val threeDView = ThreeDView(viewModel)

    override fun buildContent() {
        threeDView.buildContent()
        element = document.create.div()
    }

    override fun onAttachToRoot(rootElement: Element) {
        super.onAttachToRoot(rootElement)
        threeDView.attachTo(element.ref)
    }
}