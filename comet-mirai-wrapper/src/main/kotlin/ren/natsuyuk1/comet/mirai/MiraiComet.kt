package ren.natsuyuk1.comet.mirai

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
import ren.natsuyuk1.comet.mirai.util.runWithScope
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

        miraiBot = BotFactory.newBot(qq = this.config.id, password = this.config.password, configuration = config)

        cl.runWithScope(scope) {
            miraiBot.login()

            miraiBot.eventChannel.subscribeAlways<net.mamoe.mirai.event.Event> {
                cl.runWithSuspend {
                    it.redirectToComet(this@MiraiComet)
                }
            }

            miraiBot.join()
        }

        logger.info { "Mirai ${miraiBot.id} 登录成功" }
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
