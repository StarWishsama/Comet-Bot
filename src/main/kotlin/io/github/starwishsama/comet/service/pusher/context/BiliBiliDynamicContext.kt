/*
 * Copyright (c) 2019-2021 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package io.github.starwishsama.comet.service.pusher.context

import com.fasterxml.jackson.annotation.JsonProperty
import io.github.starwishsama.comet.api.thirdparty.bilibili.DynamicApi
import io.github.starwishsama.comet.api.thirdparty.bilibili.data.dynamic.convertToWrapper
import io.github.starwishsama.comet.objects.push.BiliBiliUser
import io.github.starwishsama.comet.objects.wrapper.MessageWrapper

class BiliBiliDynamicContext(
    pushTarget: MutableList<Long>,
    retrieveTime: Long,
    @JsonProperty("custom_status")
    override var status: PushStatus = PushStatus.READY,
    val pushUser: BiliBiliUser,
    @JsonProperty("dynamic_id")
    var dynamicId: Long,
) : PushContext(pushTarget, retrieveTime, status), Pushable {
    override fun toMessageWrapper(): MessageWrapper {
        val before = DynamicApi.getDynamicById(dynamicId).convertToWrapper()
        return MessageWrapper().addText(
            "${pushUser.userName}\n"
        ).setUsable(before.isUsable()).addElements(before.getMessageContent())
    }

    override fun contentEquals(other: PushContext): Boolean {
        if (other !is BiliBiliDynamicContext) return false

        return dynamicId == other.dynamicId
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