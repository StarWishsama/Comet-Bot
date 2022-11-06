package ren.natsuyuk1.comet.mirai

import mu.KotlinLogging.logger
import net.mamoe.mirai.Bot
import net.mamoe.mirai.BotFactory
import net.mamoe.mirai.contact.PermissionDeniedException
import net.mamoe.mirai.internal.network.Packet
import net.mamoe.mirai.message.data.MessageSource.Key.recall
import net.mamoe.mirai.network.NoStandardInputForCaptchaException
import net.mamoe.mirai.utils.*
import org.jline.reader.LineReader
import ren.natsuyuk1.comet.api.Comet
import ren.natsuyuk1.comet.api.attachMessageProcessor
import ren.natsuyuk1.comet.api.config.CometConfig
import ren.natsuyuk1.comet.api.message.MessageSource
import ren.natsuyuk1.comet.api.platform.LoginPlatform
import ren.natsuyuk1.comet.api.user.Group
import ren.natsuyuk1.comet.commands.service.subscribePushTemplateEvent
import ren.natsuyuk1.comet.listener.registerListeners
import ren.natsuyuk1.comet.mirai.config.MiraiConfig
import ren.natsuyuk1.comet.mirai.config.toMiraiProtocol
import ren.natsuyuk1.comet.mirai.contact.toCometGroup
import ren.natsuyuk1.comet.mirai.event.redirectToComet
import ren.natsuyuk1.comet.mirai.util.LoggerRedirector
import ren.natsuyuk1.comet.mirai.util.runWith
import ren.natsuyuk1.comet.mirai.util.runWithScope
import ren.natsuyuk1.comet.mirai.util.runWithSuspend
import ren.natsuyuk1.comet.service.subscribeGitHubEvent
import ren.natsuyuk1.comet.utils.coroutine.ModuleScope
import java.awt.Desktop

private val logger = logger("Comet-Mirai")

class MiraiComet(
    /**
     * 一个 Comet 实例的 [CometConfig]
     */
    config: CometConfig,

    private val cl: ClassLoader,

    private val miraiConfig: MiraiConfig,

    private val reader: LineReader
) : Comet(LoginPlatform.MIRAI, config, logger, ModuleScope("mirai (${miraiConfig.id})")) {
    lateinit var miraiBot: Bot

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
                SwingSolver
            } else {
                StandardCharImageLoginSolver(input = {
                    try {
                        reader.readLine()
                    } catch (e: Exception) {
                        throw NoStandardInputForCaptchaException(e)
                    }
                })
            }
        }

        cl.runWithScope(scope) {
            try {
                if (!::miraiBot.isInitialized) {
                    miraiBot = BotFactory.newBot(
                        qq = this.config.id,
                        password = this.config.password,
                        configuration = config
                    )

                    miraiBot.eventChannel
                        .parentScope(scope)
                        .exceptionHandler {
                            logger.warn(it) { "Mirai Bot (${miraiBot.id}) 发生异常" }
                        }
                        .subscribeAlways<net.mamoe.mirai.event.Event> {
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
        miraiBot.close()
    }

    override suspend fun getGroup(id: Long): Group? = cl.runWith { miraiBot.getGroup(id)?.toCometGroup(this) }

    override suspend fun deleteMessage(source: MessageSource): Boolean =
        cl.runWithSuspend {
            return@runWithSuspend runCatching<Boolean> {
                source as MiraiMessageSource

                source.miraiSource.recall()

                true
            }.onFailure {
                if (it !is PermissionDeniedException) {
                    logger.warn(it) { "撤回消息 $source 失败" }
                }

                return@runWithSuspend false
            }.getOrDefault(false)
        }
}
