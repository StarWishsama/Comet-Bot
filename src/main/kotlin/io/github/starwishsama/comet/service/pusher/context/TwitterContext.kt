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

import io.github.starwishsama.comet.api.thirdparty.twitter.TwitterApi
import io.github.starwishsama.comet.objects.wrapper.MessageWrapper
import io.github.starwishsama.comet.service.pusher.PushStatus

class TwitterContext(
    pushTarget: MutableSet<Long> = mutableSetOf(),
    retrieveTime: Long,
    status: PushStatus = PushStatus.PENDING,
    val twitterUserName: String,
    val tweetId: Long
) : PushContext(pushTarget, retrieveTime, status), Pushable {

    override fun toMessageWrapper(): MessageWrapper {
        val tweet = TwitterApi.getCacheByID(tweetId) ?: return MessageWrapper().setUsable(false)
        val original = tweet.toMessageWrapper()
        return MessageWrapper().addText("${tweet.user.name} 发布了一条推文\n").also {
            it.addElements(original.getMessageContent())
        }
    }

    override fun contentEquals(other: PushContext): Boolean {
        return other is TwitterContext && tweetId == other.tweetId
    }
}