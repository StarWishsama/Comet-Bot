package ren.natsuyuk1.comet.mirai

import mu.KotlinLogging.logger
import net.mamoe.mirai.Bot
import net.mamoe.mirai.BotFactory
import net.mamoe.mirai.event.Event
import net.mamoe.mirai.internal.network.Packet
import net.mamoe.mirai.message.data.MessageSource.Key.quote
import net.mamoe.mirai.message.data.MessageSource.Key.recall
import net.mamoe.mirai.message.data.OnlineMessageSource
import net.mamoe.mirai.network.NoStandardInputForCaptchaException
import net.mamoe.mirai.utils.*
import ren.natsuyuk1.comet.api.Comet
import ren.natsuyuk1.comet.api.attachMessageProcessor
import ren.natsuyuk1.comet.api.config.CometConfig
import ren.natsuyuk1.comet.api.message.MessageReceipt
import ren.natsuyuk1.comet.api.message.MessageSource
import ren.natsuyuk1.comet.api.message.MessageWrapper
import ren.natsuyuk1.comet.api.platform.CometPlatform
import ren.natsuyuk1.comet.api.user.Group
import ren.natsuyuk1.comet.api.user.User
import ren.natsuyuk1.comet.commands.service.subscribePushTemplateEvent
import ren.natsuyuk1.comet.listener.registerListeners
import ren.natsuyuk1.comet.mirai.config.MiraiConfig
import ren.natsuyuk1.comet.mirai.config.toMiraiProtocol
import ren.natsuyuk1.comet.mirai.contact.toCometGroup
import ren.natsuyuk1.comet.mirai.contact.toCometUser
import ren.natsuyuk1.comet.mirai.event.redirectToComet
import ren.natsuyuk1.comet.mirai.util.*
import ren.natsuyuk1.comet.service.subscribeGitHubEvent
import ren.natsuyuk1.comet.utils.coroutine.ModuleScope
import ren.natsuyuk1.comet.utils.system.getEnv
import java.awt.Desktop

private val logger = logger("Comet-Mirai")

class MiraiComet(
    /**
     * 一个 Comet 实例的 [CometConfig]
     */
    config: CometConfig,
    private val miraiConfig: MiraiConfig,
) : Comet(CometPlatform.MIRAI, config, logger, ModuleScope("mirai (${miraiConfig.id})")) {
    private lateinit var miraiBot: Bot
    private val cl = config.classLoader

    override val id: Long
        get() = miraiConfig.id

    @OptIn(MiraiExperimentalApi::class, MiraiInternalApi::class)
    override fun login() {
        val config = BotConfiguration.Default.apply {
            botLoggerSupplier = { it ->
                LoggerRedirector(logger("mirai (${it.id})"))
            }
            networkLoggerSupplier = { it ->
                LoggerRedirector(logger("mirai-net (${it.id})"))
            }

            fileBasedDeviceInfo()

            protocol = miraiConfig.protocol.toMiraiProtocol()

            heartbeatStrategy = miraiConfig.heartbeatStrategy

            heartbeatPeriodMillis = miraiConfig.heartbeatPeriodMillis

            loginSolver = if (Desktop.isDesktopSupported()) {
                LoginSolver.Default
            } else if (getEnv("comet.no-terminal").isNullOrBlank()) {
                StandardCharImageLoginSolver(input = {
                    try {
                        config.reader!!.readLine()
                    } catch (e: Exception) {
                        throw NoStandardInputForCaptchaException(e)
                    }
                })
            } else {
                null
            }
        }

        cl.runWithScope(scope) {
            try {
                if (!::miraiBot.isInitialized) {
                    miraiBot = BotFactory.newBot(
                        qq = this.config.id, password = this.config.password, configuration = config
                    )

                    miraiBot.eventChannel.parentScope(scope).exceptionHandler {
                        logger.warn(it) { "Mirai Bot (${miraiBot.id}) 发生异常" }
                    }.subscribeAlways<Event> {
                        if (it is Packet.NoEventLog || it is Packet.NoLog) {
                            return@subscribeAlways
                        }

                        it.redirectToComet(this@MiraiComet)
                    }
                }

                miraiBot.login()

                if (miraiBot.isOnline) {
                    logger.info { "Mirai ${miraiBot.id} 登录成功" }
                } else {
                    logger.warn { "Mirai ${miraiBot.id} 并未正常登录" }
                }

                miraiBot.join()
            } catch (e: Exception) {
                logger.warn(e) { "Mirai 发生异常" }
            }
        }
    }

    override fun afterLogin() {
        attachMessageProcessor()
        registerListeners()
        subscribeGitHubEvent()
        subscribePushTemplateEvent()
    }

    override fun close() {
        if (::miraiBot.isInitialized) miraiBot.close()
    }

    /** Start of IComet region */

    override suspend fun getGroup(id: Long): Group? = cl.runWith { miraiBot.getGroup(id)?.toCometGroup(this) }

    override suspend fun deleteMessage(source: MessageSource): Boolean = cl.runWithSuspend {
        (source as? MiraiMessageSource ?: return@runWithSuspend false)
            .miraiSource.recall()

        return@runWithSuspend true
    }

    override suspend fun getFriend(id: Long): User? = cl.runWithSuspend {
        miraiBot.getFriend(id)?.toCometUser(this)
    }

    override suspend fun getStranger(id: Long): User? = cl.runWithSuspend {
        miraiBot.getStranger(id)?.toCometUser(this)
    }

    override suspend fun reply(message: MessageWrapper, receipt: MessageReceipt): MessageReceipt? = cl.runWithSuspend {
        val source = receipt.source as? MiraiMessageSource ?: return@runWithSuspend null
        val quoteReply = source.miraiSource.quote()

        return@runWithSuspend when (source.type) {
            MessageSource.MessageSourceType.GROUP -> {
                miraiBot.getGroup(source.from)?.let {
                    it.sendMessage(quoteReply.plus(message.toMessageChain(it)))
                }?.source?.toMessageSource()?.let { MessageReceipt(this, it) }
            }

            MessageSource.MessageSourceType.FRIEND -> {
                miraiBot.getFriend(source.from)?.let {
                    it.sendMessage(quoteReply.plus(message.toMessageChain(it)))
                }?.source?.toMessageSource()?.let { MessageReceipt(this, it) }
            }

            MessageSource.MessageSourceType.TEMP -> {
                (source.miraiSource as? OnlineMessageSource)?.subject?.let {
                    it.sendMessage(quoteReply.plus(message.toMessageChain(it)))
                }?.source?.toMessageSource()?.let { MessageReceipt(this, it) }
            }

            MessageSource.MessageSourceType.STRANGER -> {
                miraiBot.getStranger(source.from)?.let {
                    it.sendMessage(quoteReply.plus(message.toMessageChain(it)))
                }?.source?.toMessageSource()?.let { MessageReceipt(this, it) }
            }

            else -> null
        }
    }
}
