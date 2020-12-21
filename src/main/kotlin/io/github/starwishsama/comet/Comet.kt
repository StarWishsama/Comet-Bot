package io.github.starwishsama.comet

import io.github.starwishsama.comet.BotVariables.bot
import io.github.starwishsama.comet.BotVariables.cfg
import io.github.starwishsama.comet.BotVariables.consoleCommandLogger
import io.github.starwishsama.comet.BotVariables.daemonLogger
import io.github.starwishsama.comet.BotVariables.filePath
import io.github.starwishsama.comet.BotVariables.logger
import io.github.starwishsama.comet.BotVariables.startTime
import io.github.starwishsama.comet.Comet.isFailed
import io.github.starwishsama.comet.api.command.CommandExecutor
import io.github.starwishsama.comet.api.thirdparty.bilibili.BiliBiliMainApi
import io.github.starwishsama.comet.api.thirdparty.bilibili.FakeClientApi
import io.github.starwishsama.comet.api.thirdparty.twitter.TwitterApi
import io.github.starwishsama.comet.api.thirdparty.youtube.YoutubeApi
import io.github.starwishsama.comet.commands.chats.*
import io.github.starwishsama.comet.commands.console.BroadcastCommand
import io.github.starwishsama.comet.commands.console.DebugCommand
import io.github.starwishsama.comet.commands.console.StopCommand
import io.github.starwishsama.comet.file.BackupHelper
import io.github.starwishsama.comet.file.DataSetup
import io.github.starwishsama.comet.listeners.BotStatusListener
import io.github.starwishsama.comet.listeners.ConvertLightAppListener
import io.github.starwishsama.comet.listeners.GroupRelatedListener
import io.github.starwishsama.comet.listeners.RepeatListener
import io.github.starwishsama.comet.pushers.*
import io.github.starwishsama.comet.utils.*
import io.github.starwishsama.comet.utils.RuntimeUtil.getUsedMemory
import io.github.starwishsama.comet.utils.StringUtil.getLastingTimeAsString
import io.github.starwishsama.comet.utils.StringUtil.isNumeric
import io.github.starwishsama.comet.utils.network.NetUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import net.kronos.rkon.core.Rcon
import net.mamoe.mirai.Bot
import net.mamoe.mirai.BotFactory
import net.mamoe.mirai.network.ForceOfflineException
import net.mamoe.mirai.network.LoginFailedException
import net.mamoe.mirai.utils.*
import org.jline.reader.LineReader
import org.jline.reader.LineReaderBuilder
import org.jline.terminal.TerminalBuilder
import java.io.EOFException
import java.time.LocalDateTime
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.time.ExperimentalTime

object Comet {
    val console: LineReader = LineReaderBuilder
        .builder().terminal(TerminalBuilder.builder().encoding(Charsets.UTF_8).build()).appName("Comet").build()
        .apply {
            setOpt(LineReader.Option.DISABLE_EVENT_EXPANSION)
            unsetOpt(LineReader.Option.INSERT_TAB)
        }
    var isFailed = false

    @ExperimentalTime
    fun startUpTask() {
        val apis = arrayOf(BiliBiliMainApi, TwitterApi, YoutubeApi)

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

        TaskUtil.runScheduleTaskAsync(25, 25, TimeUnit.MINUTES) { apis.forEach { it.resetTime() } }
        TaskUtil.runScheduleTaskAsync(5, 60 * 60, TimeUnit.SECONDS, HitokotoUpdater::run)
        TaskUtil.runScheduleTaskAsync(3, 3, TimeUnit.HOURS) {
            val usedMemoryBefore: Long = getUsedMemory()
            System.runFinalization()
            System.gc()
            daemonLogger.verbose("GC 清理成功 (${usedMemoryBefore - getUsedMemory()}) MB")
        }
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

    private fun startAllPusher(bot: Bot) {
        val pushers = arrayOf(BiliLiveChecker, TweetUpdateChecker, YoutubeStreamingChecker, BiliDynamicChecker)
        pushers.forEach {
            if (it.bot == null) it.bot = bot
            val future = TaskUtil.runScheduleTaskAsync(it.delayTime, it.internal, TimeUnit.MINUTES) {
                // Bot 不处于在线状态, 等待下一次推送时再试
                if (!bot.isOnline) return@runScheduleTaskAsync

                it.retrieve()
            }
            it.future = future
        }
    }

    @ExperimentalTime
    fun invokePostTask(bot: Bot, logger: MiraiLogger = BotVariables.logger) {
        DataSetup.initPerGroupSetting(bot)

        setupRCon()

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
                // Console Command
                StopCommand(),
                DebugCommand(),
                io.github.starwishsama.comet.commands.console.AdminCommand(),
                BroadcastCommand()
            )
        )

        logger.info("[命令] 已注册 " + CommandExecutor.countCommands() + " 个命令")

        /** 监听器 */
        val listeners = arrayOf(ConvertLightAppListener, RepeatListener, GroupRelatedListener, BotStatusListener)

        listeners.forEach {
            it.register(bot)
            logger.info("[监听器] 已注册 ${it.getName()} 监听器")
        }

        startUpTask()
        startAllPusher(bot)

        logger.info("彗星 Bot 启动成功, 耗时 ${startTime.getLastingTimeAsString()}")

        CommandExecutor.startHandler(bot)
    }

    @OptIn(MiraiExperimentalApi::class, MiraiInternalApi::class)
    @ExperimentalTime
    suspend fun startBot(qqId: Long, password: String) {
        daemonLogger.info("正在设置登录配置...")
        val config = BotConfiguration.Default.apply {
            botLoggerSupplier = { it ->
                PlatformLogger("Comet ${it.id}") {
                    BotVariables.log.writeString(BotVariables.log.getContext() + "$it\n")
                    println(it)
                }
            }
            networkLoggerSupplier = { it ->
                PlatformLogger("CometNet ${it.id}") {
                    BotVariables.log.writeString(BotVariables.log.getContext() + "$it\n")
                    println(it)
                }
            }
            heartbeatPeriodMillis = cfg.heartBeatPeriod * 60 * 1000
            fileBasedDeviceInfo()
            protocol = cfg.botProtocol
            fileCacheStrategy = FileCacheStrategy.TempCache(FileUtil.getCacheFolder())
        }
        bot = BotFactory.newBot(qq = qqId, password = password, configuration = config)
        logger.info("登录中... 使用协议 ${bot.configuration.protocol.name}")

        try {
            bot.login()
        } catch (e: LoginFailedException) {
            daemonLogger.info("登录失败, 如果是密码错误, 请重新输入密码")
            isFailed = true
            handleLogin()
            return
        }

        invokePostTask(bot)

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

    println(
    """
        
           ______                     __ 
          / ____/___  ____ ___  ___  / /_
         / /   / __ \/ __ `__ \/ _ \/ __/
        / /___/ /_/ / / / / / /  __/ /_  
        \____/\____/_/ /_/ /_/\___/\__/  


    """
    )

    DataSetup.init()
    NetUtil.initDriver()
}

@OptIn(ExperimentalTime::class)
@ExperimentalStdlibApi
suspend fun main() {
    initResources()

    Runtime.getRuntime().addShutdownHook(Thread { invokeWhenClose() })

    if (cfg.botId == 0L) {
        handleLogin()
    } else {
        daemonLogger.info("检测到登录数据, 正在自动登录账号 ${cfg.botId}")
        Comet.startBot(cfg.botId, cfg.botPassword)
    }
}

@OptIn(ExperimentalTime::class)
private suspend fun handleLogin() {
    daemonLogger.info("请输入欲登录的机器人账号")
    while (true) {
        try {
            var command: String

            if (BotVariables.isBotInitialized() && bot.isOnline) {
                break
            }

            if (cfg.botId == 0L) {
                command = Comet.console.readLine(">")
                if (command.isNumeric()) {
                    cfg.botId = command.toLong()
                    daemonLogger.info("成功设置账号为 ${cfg.botId}")
                    daemonLogger.info("请输入欲登录的机器人密码")
                }
            } else if (cfg.botPassword.isEmpty() || isFailed) {
                command = Comet.console.readLine(">", '*')
                cfg.botPassword = command
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
    NetUtil.closeDriver()
    DataSetup.saveAllResources()
    BotVariables.service.shutdown()
    BotVariables.rCon?.disconnect()
}