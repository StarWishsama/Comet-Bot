package io.github.starwishsama.comet.service.pusher.context

import io.github.starwishsama.comet.objects.wrapper.MessageWrapper

/**
 * [PushContext]
 *
 * 推送内容
 */
abstract class PushContext(
    private val pushTarget: MutableList<Long>,
    var retrieveTime: Long,
    open var status: PushStatus
) {
    abstract fun toMessageWrapper(): MessageWrapper

    abstract fun compareTo(other: PushContext): Boolean

    fun addPushTarget(id: Long) {
        if (!pushTarget.contains(id)) {
            pushTarget.add(id)
        }
    }

    fun clearPushTarget() {
        pushTarget.clear()
    }

    fun getPushTarget(): MutableList<Long> = pushTarget
}

enum class PushStatus {
    READY, FINISHED
}