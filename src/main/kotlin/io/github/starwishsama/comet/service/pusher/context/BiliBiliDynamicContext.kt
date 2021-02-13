package io.github.starwishsama.comet.service.pusher.context

import com.google.gson.annotations.SerializedName
import io.github.starwishsama.comet.api.thirdparty.bilibili.BiliBiliMainApi
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
    var dynamicID: Long = -1,
) : PushContext(pushTarget, retrieveTime, status), Pushable {

    init {
        if (dynamicID < 0) {
            dynamicID = dynamic.data.cards?.get(0)?.description?.dynamicId ?: -1
        }
    }

    fun initDynamic() {
        if (dynamicID > 0) {
            dynamic = BiliBiliMainApi.getDynamicById(dynamicID)
        }
    }

    override fun toMessageWrapper(): MessageWrapper {
        val before = dynamic.convertToWrapper()
        return MessageWrapper().addText(
            "${pushUser.userName}\n" + before.getAllText()
        ).setUsable(before.isUsable()).also {
            it.addElements(before.getMessageContent())
        }
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