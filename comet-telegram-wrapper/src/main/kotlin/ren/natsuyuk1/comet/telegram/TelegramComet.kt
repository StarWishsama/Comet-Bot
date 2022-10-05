package ren.natsuyuk1.comet.telegram

import com.soywiz.klock.DateTime
import dev.inmo.tgbotapi.bot.TelegramBot
import dev.inmo.tgbotapi.bot.exceptions.CommonRequestException
import dev.inmo.tgbotapi.bot.ktor.telegramBot
import dev.inmo.tgbotapi.extensions.api.bot.getMe
import dev.inmo.tgbotapi.extensions.api.chat.get.getChat
import dev.inmo.tgbotapi.extensions.api.deleteMessage
import dev.inmo.tgbotapi.extensions.behaviour_builder.buildBehaviourWithLongPolling
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onContentMessage
import dev.inmo.tgbotapi.extensions.utils.updates.retrieving.flushAccumulatedUpdates
import dev.inmo.tgbotapi.types.chat.GroupChat
import dev.inmo.tgbotapi.types.chat.PrivateChat
import dev.inmo.tgbotapi.types.toChatId
import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.engine.cio.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import ren.natsuyuk1.comet.api.Comet
import ren.natsuyuk1.comet.api.attachMessageProcessor
import ren.natsuyuk1.comet.api.config.CometConfig
import ren.natsuyuk1.comet.api.event.broadcast
import ren.natsuyuk1.comet.api.message.MessageSource
import ren.natsuyuk1.comet.api.platform.LoginPlatform
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
) : Comet(LoginPlatform.TELEGRAM, config, logger, ModuleScope("telegram ${config.id}")) {
    private val startTime = DateTime.now()
    lateinit var bot: TelegramBot
    override val id: Long
        get() = config.id

    override fun login() {
        bot = telegramBot(config.password) {
            this.client = HttpClient(CIO) {
                engine {
                    val proxyStr = System.getProperty("comet.proxy") ?: System.getenv("COMET_PROXY")
                    if (proxyStr.isNullOrBlank()) return@engine
                    proxy = ProxyBuilder.http(proxyStr)
                }
            }
        }

        scope.launch {
            bot.flushAccumulatedUpdates()

            logger.debug { "Refreshed accumulated updates" }

            bot.buildBehaviourWithLongPolling(scope) {
                onContentMessage({ it.chat is PrivateChat || it.chat is GroupChat }) {
                    if (it.date < startTime) {
                        return@onContentMessage
                    }

                    logger.trace { "onContentMessage > " + it.format() }
                    scope.launch {
                        it.toCometEvent(this@TelegramComet)?.broadcast()
                    }
                }
            }.join()
        }

        logger.info { "成功登录 Telegram Bot (${runBlocking { bot.getMe().username.username }})" }
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
        return try {
            val chat = bot.getChat(id.toChatId())

            if (chat is GroupChat) {
                chat.toCometGroup(this)
            } else {
                null
            }
        } catch (e: CommonRequestException) {
            null
        }
    }

    override suspend fun deleteMessage(source: MessageSource): Boolean {
        return try {
            bot.deleteMessage(source.target.toChatId(), source.messageID)
        } catch (e: CommonRequestException) {
            logger.warn(e) { "撤回消息失败, source: $source" }
            return false
        }
    }
}
