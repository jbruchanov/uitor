package com.scurab.uitor.common.util

import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ObservableTest {

    private lateinit var observable: Observable<String>
    private lateinit var lifecycleOwner: HasLifecycle

    @BeforeTest
    fun setUp() {
        observable = Observable()
        lifecycleOwner = object : HasLifecycle {
            override val coroutineContext: CoroutineContext = EmptyCoroutineContext
            override val onDetachObservable: Observable<HasLifecycle> = Observable()
        }
    }

    @Test
    fun postItem_whenSubscribedBeforeValue_getsDeliveredToObserver() {
        val result = mutableListOf<String>()
        observable.observe(lifecycleOwner) { result.add(it) }
        observable.post("1")
        assertEquals("1", result.first())
        assertEquals(1, result.size)
        observable.post("2")
        assertEquals("2", result[1])
        assertEquals(2, result.size)
    }

    @Test
    fun postItem_whenSubscribedAfterValue_getsDeliveredLast() {
        val result = mutableListOf<String>()
        observable.post("1")
        observable.post("2")
        observable.observe(lifecycleOwner) { result.add(it) }
        assertEquals(1, result.size)
        assertEquals("2", result.first())
    }

    @Test
    fun postItem_whenUnsubscribed_doesNothing() {
        val result = mutableListOf<String>()
        val observer: (String) -> Unit = { result.add(it) }
        observable.observe(lifecycleOwner, observer)
        observable.post("1")
        assertEquals("1", result.first())
        assertEquals(1, result.size)
        assertTrue(observable.removeObserver(observer))
        observable.post("2")
        assertEquals(1, result.size)
    }

    @Test
    fun postLifecycleEnds_unsubscribesObserver() {
        val result = mutableListOf<String>()
        val observer: (String) -> Unit = { result.add(it) }
        observable.observe(lifecycleOwner, observer)
        observable.post("1")
        assertEquals("1", result.first())
        assertEquals(1, result.size)
        lifecycleOwner.onDetachObservable.post(lifecycleOwner)
        observable.post("2")
        assertEquals(1, result.size)
        assertFalse(observable.removeObserver(observer))
    }

    @Test
    fun postLifecycleEnds_removesObserverFromLifecycle() {
        val result = mutableListOf<String>()
        val observer: (String) -> Unit = { result.add(it) }
        observable.observe(lifecycleOwner, observer)
        assertEquals(1, observable.observersCount)
        assertEquals(1, lifecycleOwner.onDetachObservable.observersCount)
        lifecycleOwner.onDetachObservable.post(lifecycleOwner)
        assertEquals(0, observable.observersCount)
        assertEquals(0, lifecycleOwner.onDetachObservable.observersCount)
    }

    @Test
    fun removeObserver_withNoLifecycle_removesObserver() {
        val result = mutableListOf<String>()
        val observer: (String) -> Unit = { result.add(it) }
        observable.observe(lifecycleOwner, observer)
        assertEquals(1, observable.observersCount)
        assertEquals(1, lifecycleOwner.onDetachObservable.observersCount)

        observable.removeObserver(observer)
        assertEquals(0, observable.observersCount)
        //doesn't remove it from lifecycle as it's not passed
        assertEquals(1, lifecycleOwner.onDetachObservable.observersCount)
    }

    @Test
    fun removeObserver_withLifecycle_removesObserverAndFromLifecycle() {
        val result = mutableListOf<String>()
        val observer: (String) -> Unit = { result.add(it) }
        observable.observe(lifecycleOwner, observer)
        assertEquals(1, observable.observersCount)
        assertEquals(1, lifecycleOwner.onDetachObservable.observersCount)

        observable.removeObserver(observer, lifecycleOwner)
        assertEquals(0, observable.observersCount)
        assertEquals(0, lifecycleOwner.onDetachObservable.observersCount)
    }
}