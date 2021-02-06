package io.github.starwishsama.comet.service.pusher.context

import io.github.starwishsama.comet.objects.wrapper.MessageWrapper

/**
 * [PushContext]
 *
 * 推送内容
 */
open class PushContext(
    private val pushTarget: MutableList<Long>,
    var retrieveTime: Long,
    open var status: PushStatus
): Pushable {
    fun addPushTarget(id: Long) {
        if (!pushTarget.contains(id)) {
            pushTarget.add(id)
        }
    }

    fun clearPushTarget() {
        pushTarget.clear()
    }

    fun getPushTarget(): MutableList<Long> = pushTarget

    override fun toMessageWrapper(): MessageWrapper {
        throw UnsupportedOperationException("Base PushContext can't convert to MessageWrapper")
    }

    override fun compareTo(other: PushContext): Boolean {
        return this == other
    }
}

enum class PushStatus {
    READY, FINISHED
}