package com.scurab.uitor.web.coroutine

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel

//TODO: check why this doesn't work in commonMain, moans about Unresolved reference: kotlinx
class RememberLastItemChannel<T> private constructor(private val channel: Channel<T>) :
    ReceiveChannel<T> by channel,
    SendChannel<T> by channel {

    constructor(capacity: Int = Channel.RENDEZVOUS) : this(Channel(capacity))

    var lastItem: T? = null
        private set

    override fun offer(element: T): Boolean {
        lastItem = element
        return channel.offer(element)
    }

    override suspend fun send(element: T) {
        lastItem = element
        return channel.send(element)
    }
}