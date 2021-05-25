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
) : Pushable {
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

    override fun contentEquals(other: PushContext): Boolean {
        return this == other
    }
}

enum class PushStatus {
    READY, FINISHED
}