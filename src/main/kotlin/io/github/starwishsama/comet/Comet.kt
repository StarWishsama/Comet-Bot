package io.github.starwishsama.comet

import io.github.starwishsama.comet.api.bilibili.BiliBiliApi
import io.github.starwishsama.comet.api.bilibili.FakeClientApi
import io.github.starwishsama.comet.api.twitter.TwitterApi
import io.github.starwishsama.comet.api.youtube.YoutubeApi
import io.github.starwishsama.comet.commands.MessageHandler
import io.github.starwishsama.comet.commands.subcommands.chats.*
import io.github.starwishsama.comet.commands.subcommands.console.DebugCommand
import io.github.starwishsama.comet.commands.subcommands.console.StopCommand
import io.github.starwishsama.comet.file.BackupHelper
import io.github.starwishsama.comet.file.DataSetup
import io.github.starwishsama.comet.listeners.ConvertLightAppListener
import io.github.starwishsama.comet.listeners.RepeatListener
import io.github.starwishsama.comet.tasks.BiliLiveChecker
import io.github.starwishsama.comet.tasks.HitokotoUpdater
import io.github.starwishsama.comet.tasks.TweetUpdateChecker
import io.github.starwishsama.comet.utils.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import net.kronos.rkon.core.Rcon
import net.mamoe.mirai.Bot
import net.mamoe.mirai.alsoLogin
import net.mamoe.mirai.event.subscribeMessages
import net.mamoe.mirai.join
import net.mamoe.mirai.message.data.EmptyMessageChain
import net.mamoe.mirai.utils.BotConfiguration
import net.mamoe.mirai.utils.PlatformLogger
import org.apache.commons.lang3.concurrent.BasicThreadFactory
import java.time.Duration
import java.time.LocalDateTime
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.system.exitProcess
import kotlin.time.ExperimentalTime
import kotlin.time.toKotlinDuration

object Comet {
    @ExperimentalTime
    fun startUpTask() {
        val apis = arrayOf(BiliBiliApi, TwitterApi, YoutubeApi)

        /** 定时任务 */
        BackupHelper.scheduleBackup()
        TaskUtil.runScheduleTaskAsync(
            { BotVariables.users.forEach { it.addTime(100) } },
            5,
            5,
            TimeUnit.HOURS
        )

        TaskUtil.runAsync({
            FakeClientApi.client.runCatching {
                val pwd = BotVariables.cfg.biliPassword
                val username = BotVariables.cfg.biliUserName

                if (pwd != null && username != null) {
                    runBlocking {
                        withContext(Dispatchers.IO) {
                            login(username = username, password = pwd)
                        }
                    }
                }
            }
        }, 5)

        TaskUtil.runScheduleTaskAsync({ apis.forEach { it.resetTime() } }, 25, 25, TimeUnit.MINUTES)
        TaskUtil.runScheduleTaskAsync(HitokotoUpdater::run, 5, 60 * 60, TimeUnit.SECONDS)
    }

    fun handleConsoleCommand() {
        TaskUtil.runAsync({
            val scanner = Scanner(System.`in`)
            var command: String
            while (scanner.hasNextLine()) {
                command = scanner.nextLine()
                runBlocking {
                    val result = MessageHandler.executeConsole(command)
                    if (result.isNotEmpty()) {
                        BotVariables.logger.info(result)
                    }
                }
            }
            scanner.close()
        }, 0)
    }

    fun setupRCon() {
        val url = BotVariables.cfg.rConUrl
        val pwd = BotVariables.cfg.rConPassword
        if (url != null && pwd != null && BotVariables.rCon == null) {
            BotVariables.rCon = Rcon(url, BotVariables.cfg.rConPort, pwd.toByteArray())
        }
    }

    fun startAllPusher() {
        val pushers = arrayOf(BiliLiveChecker, TweetUpdateChecker)
        pushers.forEach {
            val future = TaskUtil.runScheduleTaskAsync(it::retrieve, it.delayTime, it.cycle, TimeUnit.MINUTES)
            it.future = future
        }
    }
}

@ExperimentalTime
suspend fun main() {
    BotVariables.startTime = LocalDateTime.now()
    println(
        """
        
           ______                     __ 
          / ____/___  ____ ___  ___  / /_
         / /   / __ \/ __ `__ \/ _ \/ __/
        / /___/ /_/ / / / / / /  __/ /_  
        \____/\____/_/ /_/ /_/\___/\__/  


    """.trimIndent()
    )
    FileUtil.initLog()
    DataSetup.init()
    val qqId = BotVariables.cfg.botId
    val password = BotVariables.cfg.botPassword

    if (qqId == 0L) {
        println("请到 config.json 里填写机器人的QQ号&密码")
        exitProcess(0)
    } else {
        val config = BotConfiguration.Default
        config.botLoggerSupplier = { it ->
            PlatformLogger("Bot ${it.id}", {
                BotVariables.log.writeString(BotVariables.log.getContext() + "$it\n")
                println(it)
            })
        }
        config.networkLoggerSupplier = { it ->
            PlatformLogger("Net ${it.id}", {
                BotVariables.log.writeString(BotVariables.log.getContext() + "$it\n")
                println(it)
            })
        }
        config.heartbeatPeriodMillis = BotVariables.cfg.heartBeatPeriod * 60 * 1000
        config.fileBasedDeviceInfo()
        BotVariables.bot = Bot(qq = qqId, password = password, configuration = config)
        BotVariables.bot.alsoLogin()
        BotVariables.logger = BotVariables.bot.logger

        DataSetup.initPerGroupSetting()

        Comet.setupRCon()

        BotVariables.service = Executors.newScheduledThreadPool(
            4,
            BasicThreadFactory.Builder()
                .namingPattern("bot-service-%d")
                .daemon(true)
                .uncaughtExceptionHandler { thread, t ->
                    BotVariables.logger.warning("[定时任务] 线程 ${thread.name} 在运行时发生了错误", t)
                }.build()
        )

        MessageHandler.setupCommand(
            arrayOf(
                AdminCommand(),
                BiliBiliCommand(),
                CheckInCommand(),
                ClockInCommand(),
                io.github.starwishsama.comet.commands.subcommands.chats.DebugCommand(),
                DivineCommand(),
                GachaCommand(),
                GuessNumberCommand(),
                HelpCommand(),
                InfoCommand(),
                MusicCommand(),
                MuteCommand(),
                PictureSearch(),
                R6SCommand(),
                RConCommand(),
                KickCommand(),
                TwitterCommand(),
                VersionCommand(),
                // Console Command
                StopCommand(),
                DebugCommand()
            )
        )

        BotVariables.logger.info("[命令] 已注册 " + MessageHandler.countCommands() + " 个命令")

        /** 监听器 */
        val listeners = arrayOf(ConvertLightAppListener, RepeatListener)

        listeners.forEach {
            it.register(BotVariables.bot)
            BotVariables.logger.info("[监听器] 已注册 ${it.getName()} 监听器")
        }

        Comet.startUpTask()
        Comet.startAllPusher()

        val time = Duration.between(BotVariables.startTime, LocalDateTime.now())

        BotVariables.logger.info("彗星 Bot 启动成功, 耗时 ${time.toKotlinDuration().toFriendly()}")

        Runtime.getRuntime().addShutdownHook(Thread {
            BotVariables.logger.info("[Bot] 正在关闭 Bot...")
            DataSetup.saveFiles()
            BotVariables.service.shutdown()
            BotVariables.rCon?.disconnect()
        })

        BotVariables.bot.subscribeMessages {
            always {
                if (BotVariables.switch && sender.id != 80000000L) {
                    val result = MessageHandler.execute(this)
                    if (result !is EmptyMessageChain) {
                        reply(result)
                    }
                }
            }
        }

        Comet.handleConsoleCommand()

        BotVariables.bot.join() // 等待 Bot 离线, 避免主线程退出
    }
}
