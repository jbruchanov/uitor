package com.scurab.uitor.web.common

import com.scurab.uitor.web.inspector.InspectorViewModel
import com.scurab.uitor.web.util.HashToken
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

abstract class InspectorPage(protected val viewModel: InspectorViewModel) : Page() {

    init {
        GlobalScope.launch {
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