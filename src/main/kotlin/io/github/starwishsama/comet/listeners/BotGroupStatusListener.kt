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

import io.github.starwishsama.comet.managers.GroupConfigManager
import net.mamoe.mirai.event.Event
import net.mamoe.mirai.event.events.BotJoinGroupEvent
import net.mamoe.mirai.event.events.BotLeaveEvent
import kotlin.reflect.KClass

object BotGroupStatusListener : NListener {
    override fun listen(event: Event) {
        when (event) {
            is BotJoinGroupEvent -> {
                if (GroupConfigManager.getConfig(event.group.id) == null) {
                    GroupConfigManager.createNewConfig(event.group.id)
                }
            }
            is BotLeaveEvent -> {
                if (GroupConfigManager.getConfig(event.group.id) != null) {
                    GroupConfigManager.expireConfig(event.group.id)
                }
            }
        }
    }

    override val eventToListen: List<KClass<out Event>> = listOf(BotJoinGroupEvent::class, BotLeaveEvent::class)

    override fun getName(): String = "群聊"
}