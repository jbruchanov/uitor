@file:Suppress("UNCHECKED_CAST")

package com.scurab.uitor.common.util

private val NULL = Unit

interface IObservable<T> {
    val item: T?
    fun post(item: T)
    fun observe(lifecycle: HasLifecycle, observer: (T) -> Unit)
    fun removeObserver(observer: (T) -> Unit, lifecycle: HasLifecycle? = null): Boolean
}

open class Observable<T> : IObservable<T> {

    private val observers = mutableSetOf<ObserverWrapper<T>>()
    private var _item: T = NULL as T
    override val item: T? get() = _item.takeIf { it != NULL }

    val observersCount get() = observers.size

    override fun post(item: T) {
        _item = item
        notifyObservers()
    }

    private fun observe(observer: ObserverWrapper<T>) {
        observers.add(observer)
    }

    private fun removeObserver(observer: ObserverWrapper<T>): Boolean {
        return observers.remove(observer)
    }

    override fun observe(lifecycle: HasLifecycle, observer: (T) -> Unit) {
        val wrapper = LifecycleHandlingObserver(lifecycle, this, observer)
        observe(DefaultObserverWrapper(observer))
        lifecycle.onDetachObservable.observe(wrapper)
        if (_item != NULL) {
            observer(_item)
        }
    }

    override fun removeObserver(observer: (T) -> Unit, lifecycle: HasLifecycle?): Boolean {
        lifecycle?.onDetachObservable?.removeObserver(LifecycleHandlingObserver(lifecycle, this, observer))
        return observers.remove(DefaultObserverWrapper(observer))
    }

    private fun notifyObservers() {
        if (_item != NULL) {
            observers.forEach {
                it.notify(_item)
            }
        }
    }

    private class LifecycleHandlingObserver<T>(
        private val lifecycleOwner: HasLifecycle,
        private val observable: Observable<T>,
        private val _observer: (T) -> Unit
    ) : ObserverWrapper<HasLifecycle>() {
        override val observer: (HasLifecycle) -> Unit = {
            lifecycleOwner.onDetachObservable.removeObserver(this)
            observable.removeObserver(_observer)
        }

        override fun hashCode(): Int = _observer.hashCode()
        override fun equals(other: Any?): Boolean {
            return when (other) {
                is LifecycleHandlingObserver<*> -> _observer == other._observer
                is ObserverWrapper<*> -> _observer == other.observer
                else -> false
            }
        }
    }
}

//little bit hacky workaround for JS
//we can't have a class implementing just `(T) -> Unit` so we have to wrap the observer
//into class, hence the hashCode/equals overriding, so we don't have to track wrapper refs
private abstract class ObserverWrapper<T> {
    abstract val observer: (T) -> Unit
    open fun notify(item: T) {
        observer.invoke(item)
    }
}

private open class DefaultObserverWrapper<T>(
    override val observer: (T) -> Unit
) : ObserverWrapper<T>() {
    override fun hashCode(): Int = observer.hashCode()
    override fun equals(other: Any?): Boolean {
        val observer = (other as? ObserverWrapper<T>)?.observer
        return this.observer == observer
    }
}