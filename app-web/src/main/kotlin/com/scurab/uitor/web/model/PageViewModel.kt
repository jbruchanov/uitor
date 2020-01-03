package com.scurab.uitor.web.model

import com.scurab.uitor.web.App
import com.scurab.uitor.web.common.IServerApi

interface CommonViewModel {
    val screenIndex: Int
    val clientConfig: IClientConfig
    val serverApi: IServerApi
}

class PageViewModel(
    override val screenIndex: Int,
    override val clientConfig: IClientConfig = App.clientConfig,
    override val serverApi: IServerApi = App.serverApi
) : CommonViewModel