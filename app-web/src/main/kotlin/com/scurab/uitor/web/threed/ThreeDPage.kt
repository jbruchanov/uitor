package com.scurab.uitor.web.threed

import com.scurab.uitor.common.util.ref
import com.scurab.uitor.web.common.BaseViewPropertiesPage
import com.scurab.uitor.web.model.PageViewModel
import kotlinx.html.js.div
import org.w3c.dom.Element
import org.w3c.dom.HTMLElement
import kotlin.browser.window
import kotlin.math.roundToInt

class ThreeDPage(pageViewModel: PageViewModel) : BaseViewPropertiesPage(pageViewModel) {

    override var contentElement: HTMLElement? = null; private set
    private val threeDView = ThreeDView(viewModel)

    override fun buildContent() {
        contentElement = document.create.div()
        threeDView.buildContent()
        super.buildContent()
    }

    override fun onAttachToRoot(rootElement: Element) {
        super.onAttachToRoot(rootElement)
        threeDView.attachTo(contentElement.ref)
    }

    override fun onAttached() {
        columnsLayout.isDraggingEnabled = false
        super.onAttached()
    }

    override fun onDetached() {
        threeDView.detach()
        super.onDetached()
    }

    override fun onColumnsResize(sizes: DoubleArray) {
        threeDView.dispatchContainerSizeChanged(sizes[0], window.innerHeight.toDouble())
    }
}