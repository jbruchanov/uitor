package com.scurab.uitor.web.common

import com.scurab.uitor.common.util.elog
import com.scurab.uitor.common.util.messageSafe
import com.scurab.uitor.web.inspector.InspectorViewModel
import com.scurab.uitor.web.ui.launchWithProgressBar
import com.scurab.uitor.web.util.HashToken
import kotlinx.coroutines.CancellationException

abstract class InspectorPage(protected val viewModel: InspectorViewModel) : Page() {

    init {
        launchWithProgressBar {
            try {
                viewModel.load()
            } catch (e: Exception) {
                if (e !is CancellationException) {
                    alert(e)
                }
                elog { e.messageSafe }
            }
        }
    }

    override fun stateDescription(): String {
        return HashToken.state(HashToken.SCREEN_INDEX to viewModel.screenIndex.toString())
    }
}