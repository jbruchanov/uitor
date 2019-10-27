package com.scurab.uitor.web.model

import com.scurab.uitor.web.App
import com.scurab.uitor.web.common.ServerApi

interface CommonViewModel {
    val screenIndex: Int
    val clientConfig: ClientConfig
    val serverApi: ServerApi
}

class PageViewModel(
    override val screenIndex: Int,
    override val clientConfig: ClientConfig = App.clientConfig,
    override val serverApi: ServerApi = App.serverApi
) : CommonViewModel