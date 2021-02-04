package io.github.starwishsama.comet.service.pusher.context

import io.github.starwishsama.comet.objects.push.YoutubeUser
import io.github.starwishsama.comet.objects.wrapper.MessageWrapper

class YoutubeContext(
    pushTarget: MutableList<Long>,
    retrieveTime: Long,
    override var status: PushStatus = PushStatus.READY,
    val youtubeUser: YoutubeUser,
) : PushContext(pushTarget, retrieveTime, status) {

    override fun toMessageWrapper(): MessageWrapper {
        TODO()
    }

    override fun compareTo(other: PushContext): Boolean {
        TODO()
    }
}

fun Collection<YoutubeContext>.getContext(id: String): YoutubeContext? {
    val result = this.parallelStream().filter { id == it.youtubeUser.id }.findFirst()
    return if (result.isPresent) result.get() else null
}