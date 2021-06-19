/*
 * Copyright (c) 2019-2021 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package io.github.starwishsama.comet.listeners

import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.event.Event
import net.mamoe.mirai.event.events.BotOfflineEvent
import net.mamoe.mirai.event.events.BotOnlineEvent
import kotlin.reflect.KClass

object BotStatusListener : NListener {
    override fun listen(event: Event) {
        when (event) {
            is BotOfflineEvent -> {
                if (!event.bot.isOnline) {
                    runBlocking {
                        event.bot.login()
                    }
                }
            }
        }
    }

    override val eventToListen: List<KClass<out Event>> = listOf(BotOnlineEvent::class, BotOfflineEvent::class)

    override fun getName(): String = "群聊"
}