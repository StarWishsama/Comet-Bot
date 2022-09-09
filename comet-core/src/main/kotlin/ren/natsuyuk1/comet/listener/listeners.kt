package ren.natsuyuk1.comet.listener

import ren.natsuyuk1.comet.api.Comet
import ren.natsuyuk1.comet.api.listener.register

val DEFAULT_LISTENERS = listOf(
    KeywordListener,
)

fun Comet.registerListeners() =
    DEFAULT_LISTENERS.forEach {
        it.register(this)
    }
