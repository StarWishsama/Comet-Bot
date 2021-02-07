package io.github.starwishsama.comet.service.pusher.context

import io.github.starwishsama.comet.objects.wrapper.MessageWrapper

interface Pushable {
    fun toMessageWrapper(): MessageWrapper

    fun contentEquals(other: PushContext): Boolean
}