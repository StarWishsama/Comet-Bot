package io.github.starwishsama.comet.listeners

import io.github.starwishsama.comet.managers.GroupConfigManager
import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.events.BotJoinGroupEvent
import net.mamoe.mirai.event.events.BotLeaveEvent
import net.mamoe.mirai.event.subscribeAlways

object BotGroupStatusListener: NListener {
    override fun register(bot: Bot) {
        bot.subscribeAlways<BotJoinGroupEvent> {
            if (GroupConfigManager.getConfig(group.id) == null) {
                GroupConfigManager.createNewConfig(group.id)
            }
        }

        bot.subscribeAlways<BotLeaveEvent> {
            if (GroupConfigManager.getConfig(group.id) != null) {
                GroupConfigManager.expireConfig(group.id)
            }
        }
    }

    override fun getName(): String = "群聊"
}