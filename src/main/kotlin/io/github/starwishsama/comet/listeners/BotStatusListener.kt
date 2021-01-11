package io.github.starwishsama.comet.listeners

import io.github.starwishsama.comet.BotVariables.users
import io.github.starwishsama.comet.BotVariables.yyMMddPattern
import io.github.starwishsama.comet.enums.UserLevel
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.ConcurrencyKind
import net.mamoe.mirai.event.EventPriority
import net.mamoe.mirai.event.events.BotOnlineEvent
import net.mamoe.mirai.event.globalEventChannel
import java.time.LocalDateTime
import kotlin.coroutines.EmptyCoroutineContext

object BotStatusListener: NListener {
    override fun register(bot: Bot) {
        bot.globalEventChannel().subscribeAlways(
            BotOnlineEvent::class,
            EmptyCoroutineContext,
            ConcurrencyKind.CONCURRENT,
            EventPriority.NORMAL
        ) {
            users.stream().filter { it.level == UserLevel.OWNER }.findFirst().ifPresent {
                val f = bot.getFriend(it.id)
                runBlocking { f?.sendMessage("Comet 已上线, 在 ${yyMMddPattern.format(LocalDateTime.now())}") }
            }
        }
    }

    override fun getName(): String = "Comet 状态监控"
}