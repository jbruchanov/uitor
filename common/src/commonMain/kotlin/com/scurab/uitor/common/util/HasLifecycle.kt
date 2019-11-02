package com.scurab.uitor.common.util

import kotlinx.coroutines.CoroutineScope

interface HasLifecycle : CoroutineScope {
    val onDetachObservable: Observable<HasLifecycle>
}