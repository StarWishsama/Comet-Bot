package io.github.starwishsama.comet

import io.github.starwishsama.comet.startup.CometRuntime
import io.github.starwishsama.comet.startup.LoginSolver
import io.github.starwishsama.comet.startup.LoginStatus
import io.github.starwishsama.comet.utils.FileUtil
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.mamoe.mirai.Bot
import net.mamoe.mirai.BotFactory
import net.mamoe.mirai.Mirai
import net.mamoe.mirai.network.LoginFailedException
import net.mamoe.mirai.utils.BotConfiguration
import net.mamoe.mirai.utils.FileCacheStrategy
import net.mamoe.mirai.utils.MiraiInternalApi
import net.mamoe.mirai.utils.PlatformLogger

class Comet {
    private val loginSolver: LoginSolver = LoginSolver(this)
    private lateinit var bot: Bot
    var id: Long = 0
    lateinit var password: String

    @OptIn(MiraiInternalApi::class)
    suspend fun login() {
        loginSolver.solve()

        BotVariables.daemonLogger.info("正在设置登录配置...")

        val config = BotConfiguration.Default.apply {
            botLoggerSupplier = { it ->
                PlatformLogger("Comet ${it.id}") {
                    CometApplication.console.printAbove(it)
                    BotVariables.loggerAppender.appendLog(it)
                }
            }
            networkLoggerSupplier = { it ->
                PlatformLogger("CometNet ${it.id}") {
                    CometApplication.console.printAbove(it)
                    BotVariables.loggerAppender.appendLog(it)
                }
            }
            heartbeatPeriodMillis = BotVariables.cfg.heartBeatPeriod * 60 * 1000
            fileBasedDeviceInfo()
            protocol = BotVariables.cfg.botProtocol
        }
        bot = BotFactory.newBot(qq = id, password = password, configuration = config)
        Mirai.FileCacheStrategy = FileCacheStrategy.TempCache(FileUtil.getCacheFolder())
        BotVariables.logger.info("登录中... 使用协议 ${bot.configuration.protocol.name}")


        try {
            loginSolver.status = LoginStatus.LOGIN_SUCCESS
            bot.login()
            CometRuntime.setupBot(bot, bot.logger)
        } catch (e: LoginFailedException) {
            BotVariables.daemonLogger.info("登录失败! 返回的失败信息: ${e.message}")
            loginSolver.status = LoginStatus.LOGIN_FAILED
            GlobalScope.launch {
                loginSolver.solve()
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