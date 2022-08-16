package ren.natsuyuk1.comet.telegram

import dev.inmo.tgbotapi.bot.TelegramBot
import dev.inmo.tgbotapi.bot.ktor.telegramBot
import dev.inmo.tgbotapi.extensions.api.chat.get.getChat
import dev.inmo.tgbotapi.extensions.behaviour_builder.buildBehaviourWithLongPolling
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.CommonMessageFilter
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onContentMessage
import dev.inmo.tgbotapi.extensions.utils.updates.retrieving.flushAccumulatedUpdates
import dev.inmo.tgbotapi.types.chat.GroupChat
import dev.inmo.tgbotapi.types.chat.PrivateChat
import dev.inmo.tgbotapi.types.toChatId
import kotlinx.coroutines.launch
import ren.natsuyuk1.comet.api.Comet
import ren.natsuyuk1.comet.api.attachMessageProcessor
import ren.natsuyuk1.comet.api.config.CometConfig
import ren.natsuyuk1.comet.api.event.broadcast
import ren.natsuyuk1.comet.api.user.Group
import ren.natsuyuk1.comet.listener.registerListeners
import ren.natsuyuk1.comet.service.subscribeGithubEvent
import ren.natsuyuk1.comet.telegram.contact.toCometGroup
import ren.natsuyuk1.comet.telegram.event.toCometEvent
import ren.natsuyuk1.comet.telegram.util.format
import ren.natsuyuk1.comet.utils.coroutine.ModuleScope

private val logger = mu.KotlinLogging.logger("Comet-Telegram")

class TelegramComet(
    /**
     * 一个 Comet 实例的 [CometConfig]
     */
    config: CometConfig
) : Comet(config, logger, ModuleScope("telegram ${config.id}")) {
    lateinit var bot: TelegramBot
    override val id: Long
        get() = config.id

    override fun login() {
        bot = telegramBot(config.password)

        scope.launch {
            bot.flushAccumulatedUpdates()

            logger.debug { "Refreshed accumulated updates" }

            bot.buildBehaviourWithLongPolling(scope) {
                onContentMessage(CommonMessageFilter {
                    it.chat is PrivateChat || it.chat is GroupChat
                }) {
                    logger.trace { it.format() }
                    scope.launch { it.toCometEvent(this@TelegramComet, false)?.broadcast() }
                }
            }.join()
        }

        logger.info { "成功登录 Telegram Bot ${config.id}" }
    }

    override fun afterLogin() {
        attachMessageProcessor()
        registerListeners()
        subscribeGithubEvent()
    }

    override fun close() {
        bot.close()
    }

    override suspend fun getGroup(id: Long): Group? {
        val chat = bot.getChat(id.toChatId())

        return if (chat is GroupChat) {
            chat.toCometGroup(this)
        } else {
            null
        }
    }
}
