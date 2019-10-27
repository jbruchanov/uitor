package com.scurab.uitor.web.common

import com.scurab.uitor.web.Page
import com.scurab.uitor.web.inspector.InspectorViewModel
import com.scurab.uitor.web.util.HashToken
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.browser.window

abstract class InspectorPage(protected val viewModel: InspectorViewModel) : Page() {

    init {
        GlobalScope.launch {
            try {
                viewModel.load()
            } catch (e: Exception) {
                window.alert(e.message ?: "Null message")
            }
        }
    }

    override fun stateDescription(): String {
        return HashToken.state(HashToken.SCREEN_INDEX to viewModel.screenIndex.toString())
    }
}