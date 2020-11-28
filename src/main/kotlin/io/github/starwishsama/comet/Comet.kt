package io.github.starwishsama.comet

import io.github.starwishsama.comet.BotVariables.bot
import io.github.starwishsama.comet.BotVariables.cfg
import io.github.starwishsama.comet.BotVariables.consoleCommandLogger
import io.github.starwishsama.comet.BotVariables.daemonLogger
import io.github.starwishsama.comet.BotVariables.filePath
import io.github.starwishsama.comet.BotVariables.logger
import io.github.starwishsama.comet.BotVariables.startTime
import io.github.starwishsama.comet.api.command.CommandExecutor
import io.github.starwishsama.comet.api.thirdparty.bilibili.FakeClientApi
import io.github.starwishsama.comet.api.thirdparty.bilibili.MainApi
import io.github.starwishsama.comet.api.thirdparty.twitter.TwitterApi
import io.github.starwishsama.comet.api.thirdparty.youtube.YoutubeApi
import io.github.starwishsama.comet.commands.chats.*
import io.github.starwishsama.comet.commands.console.DebugCommand
import io.github.starwishsama.comet.commands.console.StopCommand
import io.github.starwishsama.comet.file.BackupHelper
import io.github.starwishsama.comet.file.DataSetup
import io.github.starwishsama.comet.listeners.ConvertLightAppListener
import io.github.starwishsama.comet.listeners.GroupRelatedListener
import io.github.starwishsama.comet.listeners.RepeatListener
import io.github.starwishsama.comet.pushers.*
import io.github.starwishsama.comet.utils.FileUtil
import io.github.starwishsama.comet.utils.StringUtil.getLastingTimeAsString
import io.github.starwishsama.comet.utils.StringUtil.isNumeric
import io.github.starwishsama.comet.utils.TaskUtil
import io.github.starwishsama.comet.utils.getContext
import io.github.starwishsama.comet.utils.network.NetUtil
import io.github.starwishsama.comet.utils.writeString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import net.kronos.rkon.core.Rcon
import net.mamoe.mirai.Bot
import net.mamoe.mirai.alsoLogin
import net.mamoe.mirai.join
import net.mamoe.mirai.network.ForceOfflineException
import net.mamoe.mirai.network.LoginFailedException
import net.mamoe.mirai.utils.BotConfiguration
import net.mamoe.mirai.utils.FileCacheStrategy
import net.mamoe.mirai.utils.PlatformLogger
import net.mamoe.mirai.utils.secondsToMillis
import java.io.BufferedReader
import java.io.InputStreamReader
import java.time.LocalDateTime
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.time.ExperimentalTime


object Comet {
    @ExperimentalTime
    fun startUpTask() {
        val apis = arrayOf(MainApi, TwitterApi, YoutubeApi)

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
                        }
                    }
                }
            }
        }

        TaskUtil.runScheduleTaskAsync(25, 25, TimeUnit.MINUTES) { apis.forEach { it.resetTime() } }
        TaskUtil.runScheduleTaskAsync(5, 60 * 60, TimeUnit.SECONDS, HitokotoUpdater::run)
    }

    private fun handleConsoleCommand() {
        TaskUtil.runAsync {
            BufferedReader(InputStreamReader(System.`in`)).use { br ->
                var line: String
                while (br.readLine().also { line = it } != null) {
                    runBlocking {
                        val result = CommandExecutor.dispatchConsoleCommand(line)
                        if (result.isNotEmpty()) {
                            consoleCommandLogger.info(result)
                        }
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
    suspend fun startBot(qqId: Long, password: String) {
        val config = BotConfiguration.Default.apply {
            botLoggerSupplier = { it ->
                PlatformLogger("Comet ${it.id}", {
                    BotVariables.log.writeString(BotVariables.log.getContext() + "$it\n")
                    println(it)
                })
            }
            networkLoggerSupplier = { it ->
                PlatformLogger("CometNet ${it.id}", {
                    BotVariables.log.writeString(BotVariables.log.getContext() + "$it\n")
                    println(it)
                })
            }
            heartbeatPeriodMillis = (cfg.heartBeatPeriod * 60).secondsToMillis
            fileBasedDeviceInfo()
            protocol = cfg.botProtocol
            fileCacheStrategy = FileCacheStrategy.TempCache(FileUtil.getCacheFolder())
        }
        bot = Bot(qq = qqId, password = password, configuration = config).alsoLogin()

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
                        io.github.starwishsama.comet.commands.console.AdminCommand()
                )
        )

        logger.info("[命令] 已注册 " + CommandExecutor.countCommands() + " 个命令")

        /** 监听器 */
        val listeners = arrayOf(ConvertLightAppListener, RepeatListener, GroupRelatedListener)

        listeners.forEach {
            it.register(bot)
            logger.info("[监听器] 已注册 ${it.getName()} 监听器")
        }

        startUpTask()
        startAllPusher(bot)

        logger.info("彗星 Bot 启动成功, 耗时 ${startTime.getLastingTimeAsString()}")

        Runtime.getRuntime().addShutdownHook(Thread { invokeWhenClose() })

        CommandExecutor.startHandler(bot)

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
    println(
            """
        
           ______                     __ 
          / ____/___  ____ ___  ___  / /_
         / /   / __ \/ __ `__ \/ _ \/ __/
        / /___/ /_/ / / / / / /  __/ /_  
        \____/\____/_/ /_/ /_/\___/\__/  


    """
    )
    FileUtil.initLog()
    DataSetup.init()
    NetUtil.initDriver()
}

@OptIn(ExperimentalTime::class)
@ExperimentalStdlibApi
suspend fun main() {
    initResources()

    val id = cfg.botId

    if (id == 0L) {
        handleLogIn()
    } else {
        daemonLogger.info("检测到登录数据, 正在自动登录账号 ${cfg.botId}")
        Comet.startBot(cfg.botId, cfg.botPassword)
    }
}

@OptIn(ExperimentalTime::class)
@Suppress("BlockingMethodInNonBlockingContext")
private suspend fun handleLogIn() {
    daemonLogger.info("请输入欲登录的机器人账号")
    BufferedReader(InputStreamReader(System.`in`)).use { br ->
        var command: String
        var isFailed = false
        while (br.readLine().also { command = it } != null) {
            if (BotVariables.isBotInitialized() && bot.isOnline) {
                break
            }
            if (cfg.botId == 0L && command.isNumeric()) {
                cfg.botId = command.toLong()
                daemonLogger.info("成功设置账号为 ${cfg.botId}")
                daemonLogger.info("请输入欲登录的机器人密码")
            } else if (cfg.botPassword.isEmpty() || isFailed) {
                cfg.botPassword = command
                daemonLogger.info("成功设置密码, 按下 Enter 启动机器人")
                isFailed = false
            } else if (cfg.botId != 0L && cfg.botPassword.isNotEmpty()) {
                daemonLogger.info("请稍等...")
                try {
                    Comet.startBot(cfg.botId, cfg.botPassword)
                } catch (e: LoginFailedException) {
                    println("登录失败: ${e.message}\n如果是密码错误, 请重新输入密码")
                    isFailed = true
                    continue
                }
                break
            }
        }
    }
}

private fun invokeWhenClose(){
    logger.info("[Bot] 正在关闭 Bot...")
    NetUtil.closeDriver()
    DataSetup.saveFiles()
    BotVariables.service.shutdown()
    BotVariables.rCon?.disconnect()
}
