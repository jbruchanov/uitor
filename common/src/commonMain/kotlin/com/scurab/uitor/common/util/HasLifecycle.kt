package com.scurab.uitor.common.util

interface HasLifecycle {
    val onDetachObservable: Observable<HasLifecycle>
}