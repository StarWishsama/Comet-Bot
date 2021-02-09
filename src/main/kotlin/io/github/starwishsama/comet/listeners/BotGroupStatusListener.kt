package io.github.starwishsama.comet.listeners

import io.github.starwishsama.comet.managers.GroupConfigManager
import net.mamoe.mirai.event.Event
import net.mamoe.mirai.event.events.BotJoinGroupEvent
import net.mamoe.mirai.event.events.BotLeaveEvent
import kotlin.reflect.KClass

object BotGroupStatusListener: NListener {
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