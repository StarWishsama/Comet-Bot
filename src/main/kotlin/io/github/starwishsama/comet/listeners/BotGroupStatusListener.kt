package io.github.starwishsama.comet.listeners

import io.github.starwishsama.comet.managers.GroupConfigManager
import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.ConcurrencyKind
import net.mamoe.mirai.event.EventPriority
import net.mamoe.mirai.event.events.BotJoinGroupEvent
import net.mamoe.mirai.event.events.BotLeaveEvent
import net.mamoe.mirai.event.globalEventChannel
import kotlin.coroutines.EmptyCoroutineContext

object BotGroupStatusListener: NListener {
    override fun register(bot: Bot) {
        bot.globalEventChannel().subscribeAlways(
            BotJoinGroupEvent::class,
            EmptyCoroutineContext,
            ConcurrencyKind.CONCURRENT,
            EventPriority.NORMAL
        ) {
            if (GroupConfigManager.getConfig(group.id) == null) {
                GroupConfigManager.createNewConfig(group.id)
            }
        }

        bot.globalEventChannel().subscribeAlways(
            BotLeaveEvent::class,
            EmptyCoroutineContext,
            ConcurrencyKind.CONCURRENT,
            EventPriority.NORMAL
        ) {
            if (GroupConfigManager.getConfig(group.id) != null) {
                GroupConfigManager.expireConfig(group.id)
            }
        }
    }

    override fun getName(): String = "群聊"
}