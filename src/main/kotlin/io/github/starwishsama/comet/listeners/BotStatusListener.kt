package io.github.starwishsama.comet.listeners

import io.github.starwishsama.comet.BotVariables.users
import io.github.starwishsama.comet.BotVariables.yyMMddPattern
import io.github.starwishsama.comet.enums.UserLevel
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.event.Event
import net.mamoe.mirai.event.events.BotOnlineEvent
import java.time.LocalDateTime

object BotStatusListener: NListener {
    override val eventToListen = listOf(BotOnlineEvent::class)

    override fun listen(event: Event) {
        if (event is BotOnlineEvent) {
            event.apply {
                users.stream().filter { it.level == UserLevel.OWNER }.findFirst().ifPresent {
                    val f = bot.getFriend(it.id)
                    runBlocking { f?.sendMessage("Comet 已上线, 在 ${yyMMddPattern.format(LocalDateTime.now())}") }
                }
            }
        }
    }

    override fun getName(): String = "Comet 状态监控"
}