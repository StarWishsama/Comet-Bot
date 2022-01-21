/*
 * Copyright (c) 2019-2022 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package ren.natsuyuk1.comet.service.pusher

import io.github.starwishsama.comet.service.pusher.pushers.BiliBiliDynamicPusher
import io.github.starwishsama.comet.service.pusher.pushers.BiliBiliLivePusher
import io.github.starwishsama.comet.service.pusher.pushers.TwitterPusher

object PusherManager {
    private val pushers = mutableSetOf(
        BiliBiliDynamicPusher(),
        BiliBiliLivePusher(),
        TwitterPusher()
    )

    fun startPushers() {
        pushers.forEach { it.start() }
    }

    fun stopPushers() {
        pushers.forEach { it.stop() }
    }

    fun getPushers(): MutableSet<CometPusher> {
        return pushers
    }

    fun getPusherByName(name: String): CometPusher? {
        getPushers().forEach {
            if (it.name == name) {
                return it
            }
        }

        return null
    }
}