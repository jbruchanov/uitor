package com.scurab.uitor.web.util

import com.scurab.uitor.common.util.Observable
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

fun <T> lazyLifecycled(initBlock: () -> T) = ResettingLazy(initBlock)

interface HasLifecycle {
    val onDetachObservable: Observable<HasLifecycle>
}

class ResettingLazy<T>(private val initBlock: () -> T) : ReadOnlyProperty<HasLifecycle, T> {
    private var item: T? = null
    private val onDetachListener: (HasLifecycle?) -> Unit = { item = null }
    override fun getValue(thisRef: HasLifecycle, property: KProperty<*>): T {
        if (item == null) {
            thisRef.onDetachObservable.observe(onDetachListener)
            item = initBlock()
        }
        return item ?: throw NullPointerException("Item is null")
    }
}