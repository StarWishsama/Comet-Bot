package io.github.starwishsama.comet.service.pusher

import io.github.starwishsama.comet.objects.wrapper.MessageWrapper

/**
 * [PushContext]
 *
 * 推送内容
 */
abstract class PushContext<T>(
    pushTarget: List<Long>,
    retrieveTime: Long,
    context: T
) {
    abstract fun toMessageWrapper(): MessageWrapper

    abstract fun compareTo(other: PushContext<T>?): Boolean
}