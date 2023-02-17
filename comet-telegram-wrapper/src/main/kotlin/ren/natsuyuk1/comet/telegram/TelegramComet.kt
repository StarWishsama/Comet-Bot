package ren.natsuyuk1.comet.telegram

import com.soywiz.klock.DateTime
import dev.inmo.tgbotapi.bot.TelegramBot
import dev.inmo.tgbotapi.bot.ktor.telegramBot
import dev.inmo.tgbotapi.extensions.api.bot.getMe
import dev.inmo.tgbotapi.extensions.api.bot.setMyCommands
import dev.inmo.tgbotapi.extensions.api.chat.get.getChat
import dev.inmo.tgbotapi.extensions.api.deleteMessage
import dev.inmo.tgbotapi.extensions.api.send.send
import dev.inmo.tgbotapi.extensions.behaviour_builder.buildBehaviourWithLongPolling
import dev.inmo.tgbotapi.extensions.utils.asPrivateChat
import dev.inmo.tgbotapi.extensions.utils.updates.retrieving.flushAccumulatedUpdates
import dev.inmo.tgbotapi.extensions.utils.userOrNull
import dev.inmo.tgbotapi.types.BotCommand
import dev.inmo.tgbotapi.types.chat.GroupChat
import dev.inmo.tgbotapi.types.message.MarkdownV2ParseMode
import dev.inmo.tgbotapi.types.message.defaultParseMode
import dev.inmo.tgbotapi.types.toChatId
import dev.inmo.tgbotapi.utils.PreviewFeature
import dev.inmo.tgbotapi.utils.TelegramAPIUrlsKeeper
import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ren.natsuyuk1.comet.api.Comet
import ren.natsuyuk1.comet.api.attachMessageProcessor
import ren.natsuyuk1.comet.api.command.CommandManager
import ren.natsuyuk1.comet.api.command.CommandNode
import ren.natsuyuk1.comet.api.config.CometConfig
import ren.natsuyuk1.comet.api.message.MessageReceipt
import ren.natsuyuk1.comet.api.message.MessageSource
import ren.natsuyuk1.comet.api.message.MessageWrapper
import ren.natsuyuk1.comet.api.platform.CometPlatform
import ren.natsuyuk1.comet.api.user.Group
import ren.natsuyuk1.comet.api.user.User
import ren.natsuyuk1.comet.commands.service.subscribePushTemplateEvent
import ren.natsuyuk1.comet.listener.registerListeners
import ren.natsuyuk1.comet.service.subscribeGitHubEvent
import ren.natsuyuk1.comet.telegram.contact.toCometGroup
import ren.natsuyuk1.comet.telegram.contact.toCometUser
import ren.natsuyuk1.comet.telegram.event.listenGroupEvent
import ren.natsuyuk1.comet.telegram.event.listenMessageEvent
import ren.natsuyuk1.comet.telegram.util.send
import ren.natsuyuk1.comet.utils.coroutine.ModuleScope
import ren.natsuyuk1.comet.utils.ktor.initProxy
import kotlin.time.Duration.Companion.seconds

private val logger = mu.KotlinLogging.logger("Comet-Telegram")

class TelegramComet(
    /**
     * 一个 Comet 实例的 [CometConfig]
     */
    config: CometConfig,
) : Comet(CometPlatform.TELEGRAM, config, logger, ModuleScope("telegram ${config.id}")) {
    internal val startTime = DateTime.now()
    internal lateinit var username: String
    lateinit var bot: TelegramBot
    lateinit var urlsKeeper: TelegramAPIUrlsKeeper

    override val id: Long
        get() = config.id

    override fun login() {
        urlsKeeper = TelegramAPIUrlsKeeper(token = config.password)
        bot = telegramBot(urlsKeeper) {
            this.client = HttpClient(CIO) {
                engine {
                    initProxy()
                }

                install(HttpTimeout) {
                    requestTimeoutMillis = 30.seconds.inWholeMilliseconds
                }
            }
        }

        scope.launch {
            bot.flushAccumulatedUpdates()

            logger.debug { "已刷新 Telegram Bot 离线时暂存的消息" }

            username = bot.getMe().username.username

            logger.info { "成功登录 Telegram Bot ($username)" }

            bot.buildBehaviourWithLongPolling {
                setMyCommands(
                    CommandManager.getCommands()
                        .filter { it.value is CommandNode }
                        .map { BotCommand(it.key, it.value.property.description) }
                        .sortedBy { it.command },
                )

                listenMessageEvent(this@TelegramComet)
                listenGroupEvent(this@TelegramComet)
            }.join()
        }
    }

    override fun afterLogin() {
        defaultParseMode = MarkdownV2ParseMode // TODO: add to config

        attachMessageProcessor()
        registerListeners()
        subscribeGitHubEvent()
        subscribePushTemplateEvent()
    }

    override fun close() {
        bot.close()
    }

    /** Start of IComet region */

    override suspend fun getGroup(id: Long): Group? {
        // Telegram group id always negative
        if (id > 0) {
            return null
        }

        val chat = bot.getChat(id.toChatId())

        return if (chat is GroupChat) {
            chat.toCometGroup(this)
        } else {
            null
        }
    }

    override suspend fun deleteMessage(source: MessageSource): Boolean =
        bot.deleteMessage(source.target.toChatId(), source.messageID)

    /**
     * 受 Telegram 设计限制, 我们只能通过发送消息尝试
     */
    @OptIn(PreviewFeature::class)
    override suspend fun getFriend(id: Long): User? {
        val chat = bot.getChat(id.toChatId()).asPrivateChat() ?: return null

        val resp = bot.send(chat, "Test")

        delay(500)

        bot.deleteMessage(chat.id, resp.messageId)

        return chat.userOrNull()?.toCometUser(this)
    }

    override suspend fun getStranger(id: Long): User? = getFriend(id)

    override suspend fun reply(message: MessageWrapper, receipt: MessageReceipt): MessageReceipt? {
        return when (receipt.source.type) {
            MessageSource.MessageSourceType.GROUP,
            MessageSource.MessageSourceType.FRIEND,
            MessageSource.MessageSourceType.BOT,
            -> {
                send(message, receipt.source.type, receipt.source.from.toChatId(), receipt.source.messageID)
            }

            else -> null
        }
    }
}
