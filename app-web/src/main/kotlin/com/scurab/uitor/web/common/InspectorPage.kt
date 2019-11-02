package com.scurab.uitor.web.common

import com.scurab.uitor.web.inspector.InspectorViewModel
import com.scurab.uitor.web.ui.launchWithProgressBar
import com.scurab.uitor.web.util.HashToken

abstract class InspectorPage(protected val viewModel: InspectorViewModel) : Page() {

    init {
        launchWithProgressBar {
            try {
                viewModel.load()
            } catch (e: Exception) {
                alert(e)
            }
        }
    }

    override fun stateDescription(): String {
        return HashToken.state(HashToken.SCREEN_INDEX to viewModel.screenIndex.toString())
    }
}