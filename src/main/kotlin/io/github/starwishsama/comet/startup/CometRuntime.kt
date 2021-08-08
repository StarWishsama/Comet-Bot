/*
 * Copyright (c) 2019-2021 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package io.github.starwishsama.comet.startup

import io.github.starwishsama.comet.BuildConfig
import io.github.starwishsama.comet.CometApplication
import io.github.starwishsama.comet.CometVariables
import io.github.starwishsama.comet.CometVariables.cfg
import io.github.starwishsama.comet.CometVariables.comet
import io.github.starwishsama.comet.CometVariables.cometServiceServer
import io.github.starwishsama.comet.CometVariables.consoleCommandLogger
import io.github.starwishsama.comet.CometVariables.daemonLogger
import io.github.starwishsama.comet.CometVariables.logger
import io.github.starwishsama.comet.api.command.CommandManager
import io.github.starwishsama.comet.api.command.MessageHandler
import io.github.starwishsama.comet.api.thirdparty.bilibili.DynamicApi
import io.github.starwishsama.comet.api.thirdparty.bilibili.VideoApi
import io.github.starwishsama.comet.api.thirdparty.twitter.TwitterApi
import io.github.starwishsama.comet.commands.chats.*
import io.github.starwishsama.comet.commands.console.BroadcastCommand
import io.github.starwishsama.comet.commands.console.DebugCommand
import io.github.starwishsama.comet.commands.console.StopCommand
import io.github.starwishsama.comet.file.DataSaveHelper
import io.github.starwishsama.comet.file.DataSetup
import io.github.starwishsama.comet.listeners.*
import io.github.starwishsama.comet.logger.HinaLogLevel
import io.github.starwishsama.comet.logger.RetrofitLogger
import io.github.starwishsama.comet.service.gacha.GachaService
import io.github.starwishsama.comet.service.pusher.PusherManager
import io.github.starwishsama.comet.service.server.CometServiceServer
import io.github.starwishsama.comet.service.task.GroupFileAutoRemover
import io.github.starwishsama.comet.utils.FileUtil
import io.github.starwishsama.comet.utils.LoggerAppender
import io.github.starwishsama.comet.utils.RuntimeUtil
import io.github.starwishsama.comet.utils.StringUtil.getLastingTimeAsString
import io.github.starwishsama.comet.utils.TaskUtil
import io.github.starwishsama.comet.utils.network.NetUtil
import kotlinx.coroutines.isActive
import net.kronos.rkon.core.Rcon
import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.globalEventChannel
import okhttp3.OkHttpClient
import org.jline.reader.EndOfFileException
import org.jline.reader.UserInterruptException
import java.net.InetSocketAddress
import java.net.Proxy
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

object CometRuntime {
    fun postSetup() {
        CometVariables.startTime = LocalDateTime.now()
        CometVariables.loggerAppender = LoggerAppender(FileUtil.getLogLocation())
        CometVariables.miraiLoggerAppender = LoggerAppender(FileUtil.getLogLocation("mirai"))
        CometVariables.miraiNetLoggerAppender = LoggerAppender(FileUtil.getLogLocation("mirai-net"))

        Runtime.getRuntime().addShutdownHook(Thread { shutdownTask() })

        logger.info(
            """
        
           ______                     __ 
          / ____/___  ____ ___  ___  / /_
         / /   / __ \/ __ `__ \/ _ \/ __/
        / /___/ /_/ / / / / / /  __/ /_  
        \____/\____/_/ /_/ /_/\___/\__/  


    """
        )

        DataSetup.init()

        CometVariables.client = OkHttpClient().newBuilder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .followRedirects(true)
            .readTimeout(5, TimeUnit.SECONDS)
            .hostnameVerifier { _, _ -> true }
            .also {
                if (cfg.proxySwitch) {
                    if (NetUtil.checkProxyUsable()) {
                        it.proxy(Proxy(cfg.proxyType, InetSocketAddress(cfg.proxyUrl, cfg.proxyPort)))
                    }
                }
            }
            .addInterceptor(RetrofitLogger())
            .build()
    }

    private fun shutdownTask() {
        logger.info("[Bot] 正在关闭 Bot...")
        DataSetup.saveAllResources()
        PusherManager.savePushers()
        cometServiceServer?.stop()
        TaskUtil.service.shutdown()
        CometVariables.rCon?.disconnect()
        CometVariables.miraiLoggerAppender.close()
        CometVariables.loggerAppender.close()
    }

    fun setupBot(bot: Bot) {
        CommandManager.setupCommands(
            arrayOf(
                AdminCommand(),
                ArkNightCommand(),
                BiliBiliCommand(),
                CheckInCommand(),
                ClockInCommand(),
                io.github.starwishsama.comet.commands.chats.DebugCommand(),
                DivineCommand(),
                GuessNumberCommand(),
                HelpCommand(),
                InfoCommand(),
                MusicCommand(),
                MuteCommand(),
                PictureSearchCommand(),
                R6SCommand(),
                RConCommand(),
                KickCommand(),
                TwitterCommand(),
                VersionCommand(),
                GroupConfigCommand(),
                RSPCommand(),
                RollCommand(),
                MinecraftCommand(),
                PusherCommand(),
                GithubCommand(),
                DiceCommand(),
                PenguinStatCommand(),
                NoAbbrCommand(),
                // Console Command
                StopCommand(),
                DebugCommand(),
                io.github.starwishsama.comet.commands.console.AdminCommand(),
                BroadcastCommand()
            )
        )

        logger.info("[命令] 已注册 " + CommandManager.countCommands() + " 个命令")

        MessageHandler.startHandler(bot)

        /** 监听器 */
        val listeners = arrayOf(
            ConvertLightAppListener,
            RepeatListener,
            BotGroupStatusListener,
            AutoReplyListener,
            GroupMemberChangedListener,
            GroupRequestListener
        )

        listeners.forEach { listener ->
            if (listener.eventToListen.isEmpty()) {
                daemonLogger.warning("监听器 ${listener::class.java.simpleName} 没有监听任何一个事件!")
            } else {
                listener.eventToListen.forEach { eventClass ->
                    bot.globalEventChannel().subscribeAlways(eventClass) {
                        if (CometVariables.switch) {
                            listener.listen(this)
                        }
                    }
                }
            }

            logger.info("[监听器] 已注册 ${listener.getName()} 监听器")
        }

        DataSetup.initPerGroupSetting(bot)

        setupRCon()

        runScheduleTasks()

        PusherManager.initPushers(bot)

        startupServer()

        logger.info("彗星 Bot 启动成功, 版本 ${BuildConfig.version}, 耗时 ${CometVariables.startTime.getLastingTimeAsString()}")

        TaskUtil.runAsync { GachaService.loadAllPools() }
    }

    fun setupRCon() {
        val address = cfg.rConUrl
        val password = cfg.rConPassword
        if (address != null && password != null && CometVariables.rCon == null) {
            CometVariables.rCon = Rcon(address, cfg.rConPort, password.toByteArray())
        }
    }

    private fun startupServer() {
        if (!cfg.webHookSwitch) {
            return
        }

        try {
            val customSuffix = cfg.webHookAddress.replace("http://", "").replace("https://", "").split("/")
            cometServiceServer = CometServiceServer(cfg.webHookPort, customSuffix.last())
        } catch (e: Exception) {
            daemonLogger.warning("Comet 服务端启动失败", e)
        }
    }

    private fun runScheduleTasks() {
        TaskUtil.runAsync { DataSaveHelper.checkOldFiles() }

        val apis = arrayOf(DynamicApi, TwitterApi, VideoApi)

        /** 定时任务 */
        DataSaveHelper.scheduleBackup()
        DataSaveHelper.scheduleSave()

        apis.forEach {
            TaskUtil.runScheduleTaskAsync(it.duration.toLong(), it.duration.toLong(), TimeUnit.HOURS) {
                it.resetTime()
            }
        }

        TaskUtil.runScheduleTaskAsync(1, 1, TimeUnit.HOURS) {
            GroupFileAutoRemover.execute()
        }

        TaskUtil.runScheduleTaskAsync(1, 1, TimeUnit.HOURS) {
            RuntimeUtil.forceGC()
        }
    }

    fun handleConsoleCommand() {
        TaskUtil.runAsync {
            consoleCommandLogger.log(HinaLogLevel.Info, "后台已启用", prefix = "后台管理")

            while (comet.getBot().isActive) {
                var line: String

                try {
                    line = CometApplication.console.readLine(">")
                    val result = MessageHandler.dispatchConsoleCommand(line)
                    if (result.isNotEmpty()) {
                        consoleCommandLogger.info(result)
                    }
                } catch (ignored: EndOfFileException) {
                } catch (ignored: UserInterruptException) {
                }

            }
        }
    }
}
