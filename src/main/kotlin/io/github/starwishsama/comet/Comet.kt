/*
 * Copyright (c) 2019-2021 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package io.github.starwishsama.comet

import io.github.starwishsama.comet.logger.HinaLogLevel
import io.github.starwishsama.comet.logger.HinaLogger
import io.github.starwishsama.comet.startup.CometLoginHelper
import io.github.starwishsama.comet.startup.CometRuntime
import io.github.starwishsama.comet.startup.LoginStatus
import io.github.starwishsama.comet.utils.FileUtil
import net.mamoe.mirai.Bot
import net.mamoe.mirai.BotFactory
import net.mamoe.mirai.Mirai
import net.mamoe.mirai.network.LoginFailedException
import net.mamoe.mirai.utils.BotConfiguration
import net.mamoe.mirai.utils.FileCacheStrategy
import net.mamoe.mirai.utils.MiraiInternalApi
import net.mamoe.mirai.utils.MiraiLoggerPlatformBase

class Comet {
    private val cometLoginHelper: CometLoginHelper = CometLoginHelper(this)
    private lateinit var bot: Bot
    var id: Long = 0
    lateinit var password: String

    @OptIn(MiraiInternalApi::class)
    suspend fun login() {
        cometLoginHelper.solve()

        CometVariables.daemonLogger.info("正在配置登录配置...")

        initBot()

        CometVariables.logger.info("登录中... 使用协议 ${bot.configuration.protocol.name}")

        try {
            cometLoginHelper.status = LoginStatus.LOGIN_SUCCESS
            bot.login()
            CometRuntime.setupBot(bot, bot.logger)
        } catch (e: LoginFailedException) {
            CometVariables.daemonLogger.warning("登录失败! 返回的失败信息: ${e.message}")
            cometLoginHelper.status = LoginStatus.LOGIN_FAILED
            cometLoginHelper.solve()
            login()
        }
    }

    suspend fun join() {
        bot.join()
    }

    fun initBot() {
        val config = BotConfiguration.Default.apply {
            botLoggerSupplier = { it ->
                CustomLogRedirecter("Mirai (${it.id})", CometVariables.miraiLogger)
            }
            networkLoggerSupplier = { it ->
                CustomLogRedirecter("MiraiNet (${it.id})", CometVariables.miraiNetLogger)
            }

            fileBasedDeviceInfo()

            protocol = CometVariables.cfg.botProtocol

            heartbeatStrategy = CometVariables.cfg.heartbeatStrategy
        }
        bot = BotFactory.newBot(qq = id, password = password, configuration = config)
        Mirai.FileCacheStrategy = FileCacheStrategy.TempCache(FileUtil.getCacheFolder())
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