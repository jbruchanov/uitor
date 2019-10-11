package com.scurab.uitor.common.render

val Int.px get() = "${this}px"
val Int.d get() = this.toDouble()
val Int.hex
    get() =
        toString(16).let {
            if (it.length == 1) {
                "0$it"
            } else it
        }.toUpperCase()