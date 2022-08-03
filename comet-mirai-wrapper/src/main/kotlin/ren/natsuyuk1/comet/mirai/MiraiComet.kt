package ren.natsuyuk1.comet.mirai

import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.Bot
import net.mamoe.mirai.BotFactory
import net.mamoe.mirai.utils.BotConfiguration
import ren.natsuyuk1.comet.api.Comet
import ren.natsuyuk1.comet.api.attachMessageProcessor
import ren.natsuyuk1.comet.api.config.CometConfig
import ren.natsuyuk1.comet.api.user.Group
import ren.natsuyuk1.comet.mirai.config.MiraiConfig
import ren.natsuyuk1.comet.mirai.config.toMiraiProtocol
import ren.natsuyuk1.comet.mirai.contact.toCometGroup
import ren.natsuyuk1.comet.mirai.event.redirectToComet
import ren.natsuyuk1.comet.mirai.util.LoggerRedirector
import ren.natsuyuk1.comet.service.subscribeGithubEvent
import ren.natsuyuk1.comet.utils.coroutine.ModuleScope


private val logger = mu.KotlinLogging.logger("Comet-Mirai")

class MiraiComet(
    /**
     * 一个 Comet 实例的 [CometConfig]
     */
    config: CometConfig,

    private val cl: ClassLoader,

    private val miraiConfig: MiraiConfig,
) : Comet(config, logger, ModuleScope("mirai (${miraiConfig.id})")) {
    lateinit var bot: Bot

    override val id: Long
        get() = miraiConfig.id

    override fun login() {
        val config = BotConfiguration.Default.apply {
            botLoggerSupplier = { it ->
                LoggerRedirector(mu.KotlinLogging.logger("mirai (${it.id})"))
            }
            networkLoggerSupplier = { it ->
                LoggerRedirector(mu.KotlinLogging.logger("mirai-net (${it.id})"))
            }

            fileBasedDeviceInfo()

            protocol = miraiConfig.protocol.toMiraiProtocol()

        }
        bot = BotFactory.newBot(qq = miraiConfig.id, password = miraiConfig.password, configuration = config)

        runBlocking {
            // Switch context class loader for mirai service loading
            Thread.currentThread().contextClassLoader = cl

            bot.login()
        }
    }

    override fun afterLogin() {
        bot.eventChannel.subscribeAlways<net.mamoe.mirai.event.Event> {
            it.redirectToComet(this@MiraiComet)
        }

        attachMessageProcessor()
        subscribeGithubEvent()
    }

    override fun close() {
        bot.close()
    }

    override fun getGroup(id: Long): Group? = bot.getGroup(id)?.toCometGroup(this)
}
