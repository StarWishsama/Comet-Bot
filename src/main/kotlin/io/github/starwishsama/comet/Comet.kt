package io.github.starwishsama.comet

import io.github.starwishsama.comet.logger.HinaLogLevel
import io.github.starwishsama.comet.logger.HinaLogger
import io.github.starwishsama.comet.startup.CometRuntime
import io.github.starwishsama.comet.startup.CometLoginHelper
import io.github.starwishsama.comet.startup.LoginStatus
import io.github.starwishsama.comet.utils.FileUtil
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.mamoe.mirai.Bot
import net.mamoe.mirai.BotFactory
import net.mamoe.mirai.Mirai
import net.mamoe.mirai.network.LoginFailedException
import net.mamoe.mirai.utils.*

class Comet {
    private val cometLoginHelper: CometLoginHelper = CometLoginHelper(this)
    private lateinit var bot: Bot
    var id: Long = 0
    lateinit var password: String

    @OptIn(MiraiInternalApi::class)
    suspend fun login() {
        cometLoginHelper.solve()

        BotVariables.daemonLogger.info("正在配置登录配置...")

        val config = BotConfiguration.Default.apply {
            botLoggerSupplier = { it ->
                CustomLogRedirecter("Mirai (${it.id})", BotVariables.logger)
            }
            networkLoggerSupplier = { it ->
                CustomLogRedirecter("MiraiNet (${it.id})", BotVariables.netLogger)
            }
            heartbeatPeriodMillis = 30.secondsToMillis
            fileBasedDeviceInfo()
            protocol = BotVariables.cfg.botProtocol
        }
        bot = BotFactory.newBot(qq = id, password = password, configuration = config)
        Mirai.FileCacheStrategy = FileCacheStrategy.TempCache(FileUtil.getCacheFolder())
        BotVariables.logger.info("登录中... 使用协议 ${bot.configuration.protocol.name}")

        try {
            cometLoginHelper.status = LoginStatus.LOGIN_SUCCESS
            bot.login()
            CometRuntime.setupBot(bot, bot.logger)
        } catch (e: LoginFailedException) {
            BotVariables.daemonLogger.warning("登录失败! 返回的失败信息: ${e.message}")
            cometLoginHelper.status = LoginStatus.LOGIN_FAILED
            GlobalScope.launch {
                cometLoginHelper.solve()
            }
            return
        }
    }

    suspend fun join() {
        bot.join()
    }

    fun getBot(): Bot {
        return bot
    }

    fun isInitialized(): Boolean {
        return ::bot.isInitialized && bot.isOnline
    }
}

internal class CustomLogRedirecter(val name: String, private val redirect: HinaLogger) : MiraiLoggerPlatformBase() {
    override val identity: String = name

    override fun debug0(message: String?, e: Throwable?) {
        redirect.log(HinaLogLevel.Debug, message, e, name)
    }

    override fun error0(message: String?, e: Throwable?) {
        redirect.log(HinaLogLevel.Error, message, e, name)
    }

    override fun info0(message: String?, e: Throwable?) {
        redirect.log(HinaLogLevel.Info, message, e, name)
    }

    override fun verbose0(message: String?, e: Throwable?) {
        redirect.log(HinaLogLevel.Verbose, message, e, name)
    }

    override fun warning0(message: String?, e: Throwable?) {
        redirect.log(HinaLogLevel.Warn, message, e, name)
    }
}