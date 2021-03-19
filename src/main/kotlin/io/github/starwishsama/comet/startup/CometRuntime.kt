package io.github.starwishsama.comet.startup

import com.hiczp.bilibili.api.retrofit.exception.BilibiliApiException
import io.github.starwishsama.comet.BotVariables
import io.github.starwishsama.comet.BotVariables.cfg
import io.github.starwishsama.comet.BotVariables.consoleCommandLogger
import io.github.starwishsama.comet.BotVariables.daemonLogger
import io.github.starwishsama.comet.BotVariables.webhookServer
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
import io.github.starwishsama.comet.listeners.*
import io.github.starwishsama.comet.logger.HinaLogLevel
import io.github.starwishsama.comet.managers.GachaManager
import io.github.starwishsama.comet.service.pusher.PusherManager
import io.github.starwishsama.comet.service.webhook.WebHookServer
import io.github.starwishsama.comet.utils.CometUtil.getRestString
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
        webhookServer?.stop()
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
                NoteCommand(),
                GithubCommand(),
                // Console Command
                StopCommand(),
                DebugCommand(),
                io.github.starwishsama.comet.commands.console.AdminCommand(),
                BroadcastCommand()
            )
        )

        logger.info("[命令] 已注册 " + CommandExecutor.countCommands() + " 个命令")

        /** 监听器 */
        val listeners = arrayOf(
            ConvertLightAppListener,
            RepeatListener,
            BotGroupStatusListener,
            AutoReplyListener,
            GroupMemberChangedListener,
            GroupRequestListener,
            NoteListener
        )

        listeners.forEach { listener ->
            if (listener.eventToListen.isEmpty()) {
                daemonLogger.warning("监听器 ${listener::class.java.simpleName} 没有监听任何一个事件, 请检查是否正确!")
            } else {
                listener.eventToListen.forEach { eventClass ->
                    bot.globalEventChannel().subscribeAlways(eventClass) {
                        if (BotVariables.switch) {
                            listener.listen(this)
                        }
                    }
                }
            }

            logger.info("[监听器] 已注册 ${listener.getName()} 监听器")
        }

        runScheduleTasks()

        PusherManager.initPushers(bot)

        startupServer()

        DataSetup.initPerGroupSetting(bot)

        logger.info("彗星 Bot 启动成功, 版本 ${BuildConfig.version}, 耗时 ${BotVariables.startTime.getLastingTimeAsString()}")

        RuntimeUtil.doGC()

        CommandExecutor.startHandler(bot)
    }

    fun setupRCon() {
        val address = cfg.rConUrl
        val password = cfg.rConPassword
        if (address != null && password != null && BotVariables.rCon == null) {
            BotVariables.rCon = Rcon(address, cfg.rConPort, password.toByteArray())
        }
    }

    private fun startupServer() {
        if (cfg.webHookSwitch) {
            val customSuffix = cfg.webHookAddress.replace("http://", "").replace("https://", "").split("/")
            webhookServer = WebHookServer(cfg.webHookPort, customSuffix.getRestString(1, "/"))
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


        val pwd = BotVariables.cfg.biliPassword
        val username = BotVariables.cfg.biliUserName

        TaskUtil.runAsync(5) {
            if (pwd != null && username != null) {
                runBlocking {
                    withContext(Dispatchers.IO) {
                        try {
                            val response = FakeClientApi.client.login(username = username, password = pwd)
                            daemonLogger.info("成功登录哔哩哔哩账号")
                        } catch (e: BilibiliApiException) {
                            when (e.commonResponse.code) {
                                -629 -> {
                                    daemonLogger.warning("哔哩哔哩账号密码错误, 请稍后在后台用 /bili login [账号] [密码] 进行进一步操作!")
                                }
                                -105 -> {
                                    daemonLogger.warning("需要滑块验证码登录, 请稍后在后台用 /bili login [账号] [密码] 进行进一步操作!")
                                }
                                else -> {
                                    daemonLogger.warning("登录失败!", e)
                                }
                            }
                        }
                    }
                }
            } else {
                daemonLogger.info("未登录哔哩哔哩账号, 部分哔哩哔哩相关功能可能受限")
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
            consoleCommandLogger.log(HinaLogLevel.Info, "后台已启用", prefix = "后台管理")

            while (true) {
                var line: String

                runBlocking {
                    line = CometApplication.console.readLine(">")
                    val result = CommandExecutor.dispatchConsoleCommand(line)
                    if (result.isNotEmpty()) {
                        consoleCommandLogger.info(result)
                    }
                }
            }
        }
    }
}