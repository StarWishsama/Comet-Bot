package io.github.starwishsama.comet.service.pusher.context

import com.google.gson.annotations.SerializedName
import io.github.starwishsama.comet.api.thirdparty.bilibili.data.dynamic.Dynamic
import io.github.starwishsama.comet.api.thirdparty.bilibili.data.dynamic.convertToWrapper
import io.github.starwishsama.comet.objects.push.BiliBiliUser
import io.github.starwishsama.comet.objects.wrapper.MessageWrapper

class BiliBiliDynamicContext(
    pushTarget: MutableList<Long>,
    retrieveTime: Long,
    @SerializedName("custom_status")
    override var status: PushStatus = PushStatus.READY,
    val pushUser: BiliBiliUser,
    @SerializedName("_dynamic")
    var dynamic: Dynamic,
) : PushContext(pushTarget, retrieveTime, status), Pushable {
    override fun toMessageWrapper(): MessageWrapper {
        val before = dynamic.convertToWrapper()
        return MessageWrapper().addText(
            "${pushUser.userName}\n"
        ).setUsable(before.isUsable()).addElements(before.getMessageContent())
    }

    override fun contentEquals(other: PushContext): Boolean {
        if (other !is BiliBiliDynamicContext) return false

        val card = dynamic.data.cards?.get(0)
        val otherCard = other.dynamic.data.cards?.get(0)

        return if (card != null && otherCard != null) {
            card.description.dynamicId == otherCard.description.dynamicId
        } else {
            false
        }
    }
}

fun Collection<PushContext>.getDynamicContext(uid: Long): BiliBiliDynamicContext? {
    for (pushContext in this) {
        if (pushContext is BiliBiliDynamicContext && pushContext.pushUser.id.toLongOrNull() == uid) {
            return pushContext
        }
    }
    return null
}