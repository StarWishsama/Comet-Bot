package ren.natsuyuk1.comet.telegram

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.text
import kotlinx.coroutines.launch
import ren.natsuyuk1.comet.api.Comet
import ren.natsuyuk1.comet.api.attachCommandManager
import ren.natsuyuk1.comet.api.config.CometConfig
import ren.natsuyuk1.comet.api.event.broadcast
import ren.natsuyuk1.comet.api.user.Group
import ren.natsuyuk1.comet.telegram.config.TelegramConfig
import ren.natsuyuk1.comet.telegram.event.toCometEvent
import ren.natsuyuk1.comet.utils.coroutine.ModuleScope

private val logger = mu.KotlinLogging.logger("Comet-Telegram")

class TelegramComet(
    /**
     * 一个 Comet 实例的 [CometConfig]
     */
    config: CometConfig,

    val telegramConfig: TelegramConfig
) : Comet(config, logger, ModuleScope("telegram ${telegramConfig.token.take(6)}")) {
    lateinit var bot: Bot

    override fun login() {
        bot = bot {
            token = telegramConfig.token
            dispatch {
                text {
                    scope.launch { toCometEvent(this@TelegramComet)?.broadcast() }
                }
            }
        }
    }

    override fun afterLogin() {
        bot.startPolling()
        attachCommandManager()
    }

    override fun close() {
        bot.stopPolling()
    }

    /**
     * Telegram Bot 并不能获取到某个群
     */
    override fun getGroup(id: Long): Group? = null
}
