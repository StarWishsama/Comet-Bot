/*
 * Copyright (c) 2019-2021 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package io.github.starwishsama.comet.service.pusher

import io.github.starwishsama.comet.service.pusher.config.EmptyPusherConfig
import io.github.starwishsama.comet.service.pusher.config.PusherConfig
import net.mamoe.mirai.Bot
import java.time.LocalDateTime

/**
 * [CometPusher]
 */
abstract class CometPusher(val bot: Bot, val name: String) {
    open var config: PusherConfig = EmptyPusherConfig()

    var retrieveTime: Int = 0

    var pushTime: Int = 0

    var latestPushTime: LocalDateTime = LocalDateTime.now()

    abstract fun retrieve()

    abstract fun push()

    abstract fun save()

    fun execute() {
        retrieve()
        push()
    }

    abstract fun start()

    fun addPushTime() {
        pushTime += 1
    }

    fun resetPushTime() {
        pushTime = 0
    }

    fun addRetrieveTime() {
        retrieveTime += 1
    }

    fun resetRetrieveTime() {
        retrieveTime = 0
    }
}