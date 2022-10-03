package ren.natsuyuk1.comet.mirai

import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging.logger
import net.mamoe.mirai.Bot
import net.mamoe.mirai.BotFactory
import net.mamoe.mirai.contact.PermissionDeniedException
import net.mamoe.mirai.message.data.MessageSource.Key.recall
import net.mamoe.mirai.utils.BotConfiguration
import ren.natsuyuk1.comet.api.Comet
import ren.natsuyuk1.comet.api.attachMessageProcessor
import ren.natsuyuk1.comet.api.config.CometConfig
import ren.natsuyuk1.comet.api.message.MessageSource
import ren.natsuyuk1.comet.api.platform.LoginPlatform
import ren.natsuyuk1.comet.api.user.Group
import ren.natsuyuk1.comet.listener.registerListeners
import ren.natsuyuk1.comet.mirai.config.MiraiConfig
import ren.natsuyuk1.comet.mirai.config.toMiraiProtocol
import ren.natsuyuk1.comet.mirai.contact.toCometGroup
import ren.natsuyuk1.comet.mirai.event.redirectToComet
import ren.natsuyuk1.comet.mirai.util.LoggerRedirector
import ren.natsuyuk1.comet.mirai.util.runWith
import ren.natsuyuk1.comet.mirai.util.runWithSuspend
import ren.natsuyuk1.comet.service.subscribeGithubEvent
import ren.natsuyuk1.comet.utils.coroutine.ModuleScope

private val logger = logger("Comet-Mirai")

class MiraiComet(
    /**
     * 一个 Comet 实例的 [CometConfig]
     */
    config: CometConfig,

    private val cl: ClassLoader,

    private val miraiConfig: MiraiConfig
) : Comet(LoginPlatform.MIRAI, config, logger, ModuleScope("mirai (${miraiConfig.id})")) {
    lateinit var miraiBot: Bot

    override val id: Long
        get() = miraiConfig.id

    override fun login() {
        if (::miraiBot.isInitialized && miraiBot.isOnline) {
            logger.warn { "Mirai Bot (${miraiBot.id}) 正常在线无需重新登录!" }
            return
        }

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
        }

        cl.runWith {
            miraiBot = BotFactory.newBot(qq = this.config.id, password = this.config.password, configuration = config)
            runBlocking {
                miraiBot.login()
            }

            miraiBot.eventChannel
                .parentScope(scope)
                .exceptionHandler {
                    logger.warn(it) { "Mirai Bot (${miraiBot.id}) 发生异常" }
                }
                .subscribeAlways<net.mamoe.mirai.event.Event> {
                    cl.runWithSuspend {
                        it.redirectToComet(this@MiraiComet)
                    }
                }

            scope.launch {
                miraiBot.join()
            }
        }

        if (miraiBot.isOnline) {
            logger.info { "Mirai ${miraiBot.id} 登录成功" }
        } else {
            logger.warn { "Mirai ${miraiBot.id} 并未正常登录" }
        }
    }

    override fun afterLogin() {
        attachMessageProcessor()
        registerListeners()
        subscribeGithubEvent()
    }

    override fun close() {
        miraiBot.close()
    }

    override suspend fun getGroup(id: Long): Group? = cl.runWith { miraiBot.getGroup(id)?.toCometGroup(this) }

    override suspend fun deleteMessage(source: MessageSource): Boolean {
        return runCatching<Boolean> {
            source as MiraiMessageSource

            source.miraiSource.recall()

            true
        }.onFailure {
            if (it !is PermissionDeniedException) {
                logger.warn(it) { "撤回消息 $source 失败" }
            }
            return false
        }.getOrDefault(false)
    }
}
