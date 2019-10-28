package com.scurab.uitor.web.util

import com.scurab.uitor.common.util.HasLifecycle
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

fun <T> lazyLifecycled(initBlock: () -> T) = ResettingLazy(initBlock)

class ResettingLazy<T>(private val initBlock: () -> T) : ReadOnlyProperty<HasLifecycle, T> {
    private var item: T? = null
    private val onDetachListener: (HasLifecycle?) -> Unit = { item = null }
    override fun getValue(thisRef: HasLifecycle, property: KProperty<*>): T {
        if (item == null) {
            thisRef.onDetachObservable.observe(thisRef, onDetachListener)
            item = initBlock()
        }
        return item ?: throw NullPointerException("Item is null")
    }
}