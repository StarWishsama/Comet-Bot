package io.github.starwishsama.comet

import io.github.starwishsama.comet.BotVariables.bot
import io.github.starwishsama.comet.BotVariables.cfg
import io.github.starwishsama.comet.BotVariables.consoleCommandLogger
import io.github.starwishsama.comet.BotVariables.daemonLogger
import io.github.starwishsama.comet.BotVariables.filePath
import io.github.starwishsama.comet.BotVariables.log
import io.github.starwishsama.comet.BotVariables.logger
import io.github.starwishsama.comet.BotVariables.loggerAppender
import io.github.starwishsama.comet.BotVariables.startTime
import io.github.starwishsama.comet.Comet.currentStep
import io.github.starwishsama.comet.api.command.CommandExecutor
import io.github.starwishsama.comet.api.thirdparty.bilibili.BiliBiliMainApi
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
import io.github.starwishsama.comet.utils.RuntimeUtil.getUsedMemory
import io.github.starwishsama.comet.utils.StringUtil.getLastingTimeAsString
import io.github.starwishsama.comet.utils.StringUtil.isNumeric
import io.github.starwishsama.comet.utils.TaskUtil
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import net.kronos.rkon.core.Rcon
import net.mamoe.mirai.Bot
import net.mamoe.mirai.BotFactory
import net.mamoe.mirai.Mirai
import net.mamoe.mirai.event.globalEventChannel
import net.mamoe.mirai.network.ForceOfflineException
import net.mamoe.mirai.network.LoginFailedException
import net.mamoe.mirai.utils.*
import org.jline.reader.LineReader
import org.jline.reader.LineReaderBuilder
import org.jline.terminal.TerminalBuilder
import java.io.EOFException
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit
import kotlin.system.exitProcess
import kotlin.time.ExperimentalTime

object Comet {
    val console: LineReader = LineReaderBuilder
        .builder().terminal(TerminalBuilder.builder().encoding(Charsets.UTF_8).build()).appName("Comet").build()
        .apply {
            setOpt(LineReader.Option.DISABLE_EVENT_EXPANSION)
            unsetOpt(LineReader.Option.INSERT_TAB)
        }
    var currentStep = -1

    @JvmStatic
    @OptIn(ExperimentalTime::class)
    @ExperimentalStdlibApi
    fun main(args: Array<String>) {
        initResources()

        Runtime.getRuntime().addShutdownHook(Thread { invokeWhenClose() })

        try {
            runBlocking {
                if (cfg.botId == 0L) {
                    handleLogin()
                } else {
                    daemonLogger.info("检测到登录数据, 正在自动登录账号 ${cfg.botId}")
                    startBot(cfg.botId, cfg.botPassword)
                }
            }
        } catch (e: CancellationException) {
            // 忽略
        }
    }


    @ExperimentalTime
    fun startAllScheduleTask() {
        val apis = arrayOf(BiliBiliMainApi, TwitterApi, YoutubeApi, VideoApi)

        /** 定时任务 */
        BackupHelper.scheduleBackup()
        TaskUtil.runScheduleTaskAsync(5, 5, TimeUnit.HOURS) {
            BotVariables.users.forEach { it.addTime(100) }
        }

        TaskUtil.runAsync(5) {
            val pwd = cfg.biliPassword
            val username = cfg.biliUserName

            if (pwd != null && username != null) {
                FakeClientApi.client.runCatching {
                    runBlocking {
                        withContext(Dispatchers.IO) {
                            login(username = username, password = pwd)
                            daemonLogger.info("成功登录哔哩哔哩账号")
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

        TaskUtil.runScheduleTaskAsync(3, 3, TimeUnit.HOURS) {
            val usedMemoryBefore: Long = getUsedMemory()
            System.runFinalization()
            System.gc()
            daemonLogger.info("定时 GC 清理成功 (-${usedMemoryBefore - getUsedMemory()} MB)")
        }

        TaskUtil.runAsync { BackupHelper.checkOldFiles() }
    }

    private fun handleConsoleCommand() {
        TaskUtil.runAsync {
            while (true) {
                var line: String

                runBlocking {
                    line = console.readLine(">")
                    val result = CommandExecutor.dispatchConsoleCommand(line)
                    if (result.isNotEmpty()) {
                        consoleCommandLogger.info(result)
                    }
                }
            }
        }
    }

    fun setupRCon() {
        val url = cfg.rConUrl
        val pwd = cfg.rConPassword
        if (url != null && pwd != null && BotVariables.rCon == null) {
            BotVariables.rCon = Rcon(url, cfg.rConPort, pwd.toByteArray())
        }
    }

    @ExperimentalTime
    fun invokePostTask(bot: Bot, logger: MiraiLogger) {
        DataSetup.initPerGroupSetting(bot)

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
                    listener.listen(this)
                }
            }

            logger.info("[监听器] 已注册 ${listener.getName()} 监听器")
        }

        startAllScheduleTask()

        PusherManager.initPushers(bot)

        logger.info("彗星 Bot 启动成功, 版本 ${BuildConfig.version}, 耗时 ${startTime.getLastingTimeAsString()}")

        CommandExecutor.startHandler(bot)
    }

    @OptIn(MiraiExperimentalApi::class, MiraiInternalApi::class)
    @ExperimentalTime
    suspend fun startBot(qqId: Long, password: String) {
        daemonLogger.info("正在设置登录配置...")

        val config = BotConfiguration.Default.apply {
            botLoggerSupplier = { it ->
                PlatformLogger("Comet ${it.id}") {
                    console.printAbove(it)
                    loggerAppender.appendLog(it)
                }
            }
            networkLoggerSupplier = { it ->
                PlatformLogger("CometNet ${it.id}") {
                    console.printAbove(it)
                    loggerAppender.appendLog(it)
                }
            }
            heartbeatPeriodMillis = cfg.heartBeatPeriod * 60 * 1000
            fileBasedDeviceInfo()
            protocol = cfg.botProtocol
        }
        bot = BotFactory.newBot(qq = qqId, password = password, configuration = config)
        Mirai.FileCacheStrategy = FileCacheStrategy.TempCache(FileUtil.getCacheFolder())
        logger.info("登录中... 使用协议 ${bot.configuration.protocol.name}")

        try {
            bot.login()
        } catch (e: LoginFailedException) {
            daemonLogger.info("登录失败, 如果是密码错误, 请重新输入密码", e)
            currentStep = 2
            handleLogin()
            return
        }

        invokePostTask(bot, logger)

        handleConsoleCommand()

        try {
            bot.join() // 等待 Bot 离线, 避免主线程退出
        } catch (e: ForceOfflineException) {
            daemonLogger.warning("账号被强制下线")
            invokeWhenClose()
            startBot(cfg.botId, cfg.botPassword)
        }
    }
}

fun initResources() {
    filePath = FileUtil.getJarLocation()
    startTime = LocalDateTime.now()
    FileUtil.initLog()
    loggerAppender = LoggerAppender(log)

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
}

@OptIn(ExperimentalTime::class)
private suspend fun handleLogin() {
    while (true) {
        try {
            var command: String

            if (BotVariables.isBotInitialized() && bot.isOnline) {
                break
            }

            if (cfg.botId == 0L || currentStep == -1) {
                daemonLogger.info("请输入欲登录的机器人账号")
                command = Comet.console.readLine(">")

                if (command == "stop") exitProcess(0)

                if (command.isNumeric()) {
                    cfg.botId = command.toLong()
                    daemonLogger.info("成功设置账号为 ${cfg.botId}")
                    currentStep = 0
                } else {
                    daemonLogger.info("请输入正确的账号")
                }
            } else if (cfg.botPassword.isEmpty() || currentStep == 2) {
                daemonLogger.info("请输入欲登录的机器人密码")
                command = Comet.console.readLine(">", '*')
                cfg.botPassword = command
                currentStep = 1
                daemonLogger.info("成功设置密码, 按下 Enter 启动机器人")
            } else if (cfg.botId != 0L && cfg.botPassword.isNotEmpty()) {
                daemonLogger.info("正在启动 Comet...")
                break
            }
        } catch (e: EOFException) {
            return
        }
    }

    Comet.startBot(cfg.botId, cfg.botPassword)
}

fun invokeWhenClose(){
    logger.info("[Bot] 正在关闭 Bot...")
    DataSetup.saveAllResources()
    PusherManager.savePushers()
    BotVariables.service.shutdown()
    BotVariables.rCon?.disconnect()
    loggerAppender.close()
}
