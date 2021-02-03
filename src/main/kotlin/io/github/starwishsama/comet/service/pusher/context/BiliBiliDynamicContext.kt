package io.github.starwishsama.comet.service.pusher.context

import io.github.starwishsama.comet.api.thirdparty.bilibili.data.dynamic.Dynamic
import io.github.starwishsama.comet.api.thirdparty.bilibili.data.dynamic.convertToWrapper
import io.github.starwishsama.comet.objects.push.BiliBiliUser
import io.github.starwishsama.comet.objects.wrapper.MessageWrapper

class BiliBiliDynamicContext(
    pushTarget: MutableList<Long>,
    retrieveTime: Long,
    status: PushStatus,
    private val pushUser: BiliBiliUser,
    val dynamic: Dynamic
) : PushContext(pushTarget, retrieveTime, status) {

    override fun toMessageWrapper(): MessageWrapper {
        val before = dynamic.convertToWrapper()
        return MessageWrapper("${pushUser.userName}\n" + before.text, success = before.success).also {
            before.pictureUrl.forEach { url ->
                it.plusImageUrl(url)
            }
        }
    }

    override fun compareTo(other: PushContext): Boolean {
        if (other !is BiliBiliDynamicContext) return false

        return (dynamic.data.card?.description?.dynamicId == other.dynamic.data.card?.description?.dynamicId)
                || (dynamic.data.cards?.get(0)?.description?.dynamicId == other.dynamic.data.cards?.get(0)?.description?.dynamicId)
    }
}