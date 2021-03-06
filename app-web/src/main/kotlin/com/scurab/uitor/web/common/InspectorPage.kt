package com.scurab.uitor.web.common

import com.scurab.uitor.common.util.elog
import com.scurab.uitor.common.util.messageSafe
import com.scurab.uitor.web.inspector.InspectorViewModel
import com.scurab.uitor.web.ui.launchWithProgressBar
import com.scurab.uitor.web.util.Browser
import com.scurab.uitor.web.util.HashToken
import kotlinx.coroutines.CancellationException

abstract class InspectorPage(protected val viewModel: InspectorViewModel) : Page() {

    override fun onAttached() {
        //keep it after init{}, otherwise navigation will explicitly hide the pbar
        launchWithProgressBar {
            try {
                viewModel.load()
            } catch (e: Exception) {
                if (e !is CancellationException) {
                    alert(
                        "${e.message ?: ""}\n" +
                                "Unable to load view hierarchy, screenIndex:${viewModel.screenIndex}\n" +
                                "Is your selected activity running?"
                    )
                }
                elog { e.messageSafe }
            }
        }
        document.addKeyUpListener {
            //global CTRL+S listener
            if (it.altKey) {
                when (it.keyCode) {
                    //hard to find something what works in chrome/FF
                    83/*s*/ -> Browser.saveButtonHandler(viewModel.clientConfig, viewModel.serverApi) { viewModel.screenIndex }(null)
                }
            }
        }
        super.onAttached()
    }

    override fun stateDescription(): String {
        return HashToken.state(HashToken.SCREEN_INDEX to viewModel.screenIndex.toString())
    }
}