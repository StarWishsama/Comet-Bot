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
import net.mamoe.mirai.event.events.BotJoinGroupEvent
import net.mamoe.mirai.event.events.BotLeaveEvent

object BotGroupStatusListener : INListener {
    override val name: String
        get() = "群变动监听"

    @EventHandler
    fun listenJoinGroup(event: BotJoinGroupEvent) {
        if (GroupConfigManager.getConfig(event.group.id) == null) {
            GroupConfigManager.createNewConfig(event.group.id)
        }
    }

    @EventHandler
    fun listenLeaveGroup(event: BotLeaveEvent) {
        if (GroupConfigManager.getConfig(event.group.id) != null) {
            GroupConfigManager.removeConfig(event.group.id)
        }
    }
}