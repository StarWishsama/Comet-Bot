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

    override fun contentEquals(other: PushContext): Boolean {
        TODO()
    }
}

fun Collection<PushContext>.getYoutubeContext(id: String): YoutubeContext? {
    val result = this.parallelStream().filter { it is YoutubeContext && id == it.youtubeUser.id }.findFirst()
    return if (result.isPresent) result.get() as YoutubeContext else null
}