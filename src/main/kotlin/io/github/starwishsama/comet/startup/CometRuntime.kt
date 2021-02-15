package io.github.starwishsama.comet.startup

import io.github.starwishsama.comet.BotVariables
import io.github.starwishsama.comet.BuildConfig
import io.github.starwishsama.comet.CometApplication
import io.github.starwishsama.comet.api.command.CommandExecutor
import io.github.starwishsama.comet.api.thirdparty.bilibili.DynamicApi
import io.github.starwishsama.comet.api.thirdparty.bilibili.FakeClientApi
import io.github.starwishsama.comet.api.thirdparty.bilibili.VideoApi
import io.github.starwishsama.comet.api.thirdparty.twitter.TwitterApi
import io.github.starwishsama.comet.api.thirdparty.youtube.YoutubeApi
import io.github.starwishsama.comet.commands.chats.*
import io.github.starwishsama.comet.commands.console.BroadcastCommand
import io.github.starwishsama.comet.commands.console.DebugCommand
import io.github.starwishsama.comet.commands.console.StopCommand
import io.github.starwishsama.comet.file.BackupHelper
import io.github.starwishsama.comet.file.DataSetup
import io.github.starwishsama.comet.listeners.AutoReplyListener
import io.github.starwishsama.comet.listeners.BotGroupStatusListener
import io.github.starwishsama.comet.listeners.ConvertLightAppListener
import io.github.starwishsama.comet.listeners.RepeatListener
import io.github.starwishsama.comet.managers.GachaManager
import io.github.starwishsama.comet.service.pusher.PusherManager
import io.github.starwishsama.comet.utils.FileUtil
import io.github.starwishsama.comet.utils.LoggerAppender
import io.github.starwishsama.comet.utils.RuntimeUtil
import io.github.starwishsama.comet.utils.StringUtil.getLastingTimeAsString
import io.github.starwishsama.comet.utils.TaskUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import net.kronos.rkon.core.Rcon
import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.globalEventChannel
import net.mamoe.mirai.utils.MiraiLogger
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

object CometRuntime {
    fun postSetup() {
        BotVariables.filePath = FileUtil.getJarLocation()
        BotVariables.startTime = LocalDateTime.now()
        FileUtil.initLog()
        BotVariables.loggerAppender = LoggerAppender(BotVariables.log)

        Runtime.getRuntime().addShutdownHook(Thread { shutdownTask() })

        BotVariables.logger.info(
            """
        
           ______                     __ 
          / ____/___  ____ ___  ___  / /_
         / /   / __ \/ __ `__ \/ _ \/ __/
        / /___/ /_/ / / / / / /  __/ /_  
        \____/\____/_/ /_/ /_/\___/\__/  


    """
        )

        DataSetup.init()
    }

    private fun shutdownTask(){
        BotVariables.logger.info("[Bot] 正在关闭 Bot...")
        DataSetup.saveAllResources()
        PusherManager.savePushers()
        BotVariables.service.shutdown()
        BotVariables.rCon?.disconnect()
        BotVariables.loggerAppender.close()
    }

    fun setupBot(bot: Bot, logger: MiraiLogger) {
        setupRCon()

        GachaManager.loadAllPools()

        CommandExecutor.setupCommand(
            arrayOf(
                AdminCommand(),
                ArkNightCommand(),
                BiliBiliCommand(),
                CheckInCommand(),
                ClockInCommand(),
                io.github.starwishsama.comet.commands.chats.DebugCommand(),
                DivineCommand(),
                PCRCommand(),
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
                YoutubeCommand(),
                MinecraftCommand(),
                PusherCommand(),
                // Console Command
                StopCommand(),
                DebugCommand(),
                io.github.starwishsama.comet.commands.console.AdminCommand(),
                BroadcastCommand()
            )
        )

        logger.info("[命令] 已注册 " + CommandExecutor.countCommands() + " 个命令")

        /** 监听器 */
        val listeners = arrayOf(ConvertLightAppListener, RepeatListener, BotGroupStatusListener, AutoReplyListener)

        listeners.forEach { listener ->
            listener.eventToListen.forEach { eventClass ->
                bot.globalEventChannel().subscribeAlways(eventClass) {
                    if (BotVariables.switch) {
                        listener.listen(this)
                    }
                }
            }

            logger.info("[监听器] 已注册 ${listener.getName()} 监听器")
        }

        runScheduleTasks()

        PusherManager.initPushers(bot)

        DataSetup.initPerGroupSetting(bot)

        logger.info("彗星 Bot 启动成功, 版本 ${BuildConfig.version}, 耗时 ${BotVariables.startTime.getLastingTimeAsString()}")

        RuntimeUtil.doGC()

        CommandExecutor.startHandler(bot)
    }

    fun setupRCon() {
        val address = BotVariables.cfg.rConUrl
        val password = BotVariables.cfg.rConPassword
        if (address != null && password != null && BotVariables.rCon == null) {
            BotVariables.rCon = Rcon(address, BotVariables.cfg.rConPort, password.toByteArray())
        }
    }

    private fun runScheduleTasks() {
        TaskUtil.runAsync { BackupHelper.checkOldFiles() }

        val apis = arrayOf(DynamicApi, TwitterApi, YoutubeApi, VideoApi)

        /** 定时任务 */
        BackupHelper.scheduleBackup()
        TaskUtil.runScheduleTaskAsync(5, 5, TimeUnit.HOURS) {
            BotVariables.users.forEach { it.addTime(100) }
        }

        TaskUtil.runAsync(5) {
            val pwd = BotVariables.cfg.biliPassword
            val username = BotVariables.cfg.biliUserName

            if (pwd != null && username != null) {
                FakeClientApi.client.runCatching {
                    runBlocking {
                        withContext(Dispatchers.IO) {
                            login(username = username, password = pwd)
                            BotVariables.daemonLogger.info("成功登录哔哩哔哩账号")
                        }
                    }
                }
            } else {
                BotVariables.daemonLogger.info("未登录哔哩哔哩账号, 部分哔哩哔哩相关功能可能受限")
            }
        }

        apis.forEach {
            TaskUtil.runScheduleTaskAsync(it.duration.toLong(), it.duration.toLong(), TimeUnit.HOURS) {
                it.resetTime()
            }
        }

        TaskUtil.runScheduleTaskAsync(1, 1, TimeUnit.HOURS) {
            RuntimeUtil.doGC()
        }
    }

    fun handleConsoleCommand() {
        TaskUtil.runAsync {
            while (true) {
                var line: String

                runBlocking {
                    line = CometApplication.console.readLine(">")
                    val result = CommandExecutor.dispatchConsoleCommand(line)
                    if (result.isNotEmpty()) {
                        BotVariables.consoleCommandLogger.info(result)
                    }
                }
            }
        }
    }
}