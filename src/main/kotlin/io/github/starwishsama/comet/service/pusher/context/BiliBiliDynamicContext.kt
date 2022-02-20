/*
 * Copyright (c) 2019-2022 StarWishsama.
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
import io.github.starwishsama.comet.api.thirdparty.bilibili.feed.toMessageWrapper
import io.github.starwishsama.comet.objects.push.BiliBiliUser
import io.github.starwishsama.comet.objects.wrapper.MessageWrapper
import io.github.starwishsama.comet.service.pusher.PushStatus
import kotlinx.coroutines.runBlocking

class BiliBiliDynamicContext(
    pushTarget: MutableSet<Long>,
    retrieveTime: Long,
    status: PushStatus = PushStatus.PENDING,
    @JsonProperty("push_user")
    val pushUser: BiliBiliUser,
    @JsonProperty("dynamic_id")
    val dynamicId: Long,
) : PushContext(pushTarget, retrieveTime, status), Pushable {
    override fun toMessageWrapper(): MessageWrapper {
        val before = runBlocking { DynamicApi.getDynamicById(dynamicId)?.toMessageWrapper() } ?: return MessageWrapper().setUsable(false)

        return MessageWrapper().addText(
            "${pushUser.userName}\n"
        ).setUsable(before.isUsable()).addElements(before.getMessageContent())
    }

    override fun contentEquals(other: PushContext): Boolean {
        return other is BiliBiliDynamicContext && dynamicId == other.dynamicId
    }
}