package ren.natsuyuk1.comet.mirai

import kotlinx.coroutines.launch
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
import ren.natsuyuk1.comet.mirai.util.runWith
import ren.natsuyuk1.comet.mirai.util.runWithSuspend
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
    lateinit var miraiBot: Bot

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

        miraiBot = BotFactory.newBot(qq = miraiConfig.id, password = miraiConfig.password, configuration = config)

        scope.launch {
            cl.runWithSuspend {
                miraiBot.login()

                miraiBot.eventChannel.subscribeAlways<net.mamoe.mirai.event.Event> {
                    cl.runWithSuspend {
                        it.redirectToComet(this@MiraiComet)
                    }
                }

                miraiBot.join()
            }
        }
    }

    override fun afterLogin() {
        attachMessageProcessor()
        subscribeGithubEvent()
    }

    override fun close() {
        miraiBot.close()
    }

    override fun getGroup(id: Long): Group? = cl.runWith { miraiBot.getGroup(id)?.toCometGroup(this) }
}
