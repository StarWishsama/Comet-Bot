package ren.natsuyuk1.comet.telegram

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.message
import com.github.kotlintelegrambot.extensions.filters.Filter
import kotlinx.coroutines.launch
import ren.natsuyuk1.comet.api.Comet
import ren.natsuyuk1.comet.api.attachMessageProcessor
import ren.natsuyuk1.comet.api.config.CometConfig
import ren.natsuyuk1.comet.api.event.broadcast
import ren.natsuyuk1.comet.api.user.Group
import ren.natsuyuk1.comet.service.subscribeGithubEvent
import ren.natsuyuk1.comet.telegram.config.TelegramConfig
import ren.natsuyuk1.comet.telegram.contact.toCometGroup
import ren.natsuyuk1.comet.telegram.event.toCometEvent
import ren.natsuyuk1.comet.telegram.util.chatID
import ren.natsuyuk1.comet.utils.coroutine.ModuleScope
import ren.natsuyuk1.comet.utils.math.NumberUtil.toInstant

private val logger = mu.KotlinLogging.logger("Comet-Telegram")

class TelegramComet(
    /**
     * 一个 Comet 实例的 [CometConfig]
     */
    config: CometConfig,

    val telegramConfig: TelegramConfig
) : Comet(config, logger, ModuleScope("telegram ${telegramConfig.token.split(":").firstOrNull() ?: "Unknown"}")) {
    lateinit var bot: Bot

    override fun login() {
        bot = bot {
            token = telegramConfig.token
            dispatch {
                message(Filter.Text) {
                    if (this.message.date.toInstant() >= initTime) {
                        logger.trace { "Incoming Telegram message: ${this.message}" }
                        scope.launch { toCometEvent(this@TelegramComet)?.broadcast() }
                    }
                }

                // When bot no access to message
                message(Filter.Command) {
                    if (this.message.date.toInstant() >= initTime) {
                        logger.trace { "Incoming Telegram command: ${this.message}" }
                        scope.launch { toCometEvent(this@TelegramComet, true)?.broadcast() }
                    }
                }
            }
        }
    }

    override fun afterLogin() {
        bot.startPolling()

        logger.info { "成功登录 Telegram Bot ${telegramConfig.token.split(":").firstOrNull()}" }

        attachMessageProcessor()
        subscribeGithubEvent()
    }

    override fun close() {
        bot.stopPolling()
    }

    /**
     * Telegram Bot 并不能获取到某个群
     */
    override fun getGroup(id: Long): Group? {
        val resp = bot.getChat(id.chatID())

        if (resp.isError) return null

        return resp.getOrNull()?.toCometGroup(this)
    }
}
