package io.github.starwishsama.comet

import io.github.starwishsama.comet.Comet.bot
import io.github.starwishsama.comet.Comet.startTime
import io.github.starwishsama.comet.api.bilibili.BiliBiliApi
import io.github.starwishsama.comet.api.bilibili.FakeClientApi
import io.github.starwishsama.comet.api.twitter.TwitterApi
import io.github.starwishsama.comet.commands.MessageHandler
import io.github.starwishsama.comet.commands.subcommands.chats.*
import io.github.starwishsama.comet.commands.subcommands.console.DebugCommand
import io.github.starwishsama.comet.commands.subcommands.console.StopCommand
import io.github.starwishsama.comet.file.BackupHelper
import io.github.starwishsama.comet.file.DataSetup
import io.github.starwishsama.comet.listeners.ConvertLightAppListener
import io.github.starwishsama.comet.listeners.RepeatListener
import io.github.starwishsama.comet.managers.TaskManager
import io.github.starwishsama.comet.tasks.BiliBiliLiveStatusChecker
import io.github.starwishsama.comet.tasks.HitokotoUpdater
import io.github.starwishsama.comet.tasks.TweetUpdateChecker
import io.github.starwishsama.comet.utils.FileUtil
import io.github.starwishsama.comet.utils.getContext
import io.github.starwishsama.comet.utils.writeString
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
import net.mamoe.mirai.utils.MiraiLogger
import net.mamoe.mirai.utils.PlatformLogger
import org.apache.commons.lang3.concurrent.BasicThreadFactory
import java.io.File
import java.time.Duration
import java.time.LocalDateTime
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import kotlin.system.exitProcess

object Comet {
    val filePath: File = File(FileUtil.getJarLocation())
    const val version = "0.3.8.1-DEV-3d17b34-20200719"
    lateinit var bot: Bot
    lateinit var startTime: LocalDateTime
    lateinit var service: ScheduledExecutorService
    lateinit var logger: MiraiLogger
    var rCon: Rcon? = null
    lateinit var log: File

    fun executeCommand() {
        val scanner = Scanner(System.`in`)
        var command: String
        while (scanner.hasNextLine()) {
            command = scanner.nextLine()
            runBlocking {
                val result = MessageHandler.executeConsole(command)
                if (result.isNotEmpty()) {
                    logger.info(result)
                }
            }
        }
        scanner.close()
    }

    fun setupRCon() {
        val url = BotVariables.cfg.rConUrl
        val pwd = BotVariables.cfg.rConPassword
        if (url != null && pwd != null && rCon == null) {
            rCon = Rcon(url, BotVariables.cfg.rConPort, pwd.toByteArray())
        }
    }

}

suspend fun main() {
    startTime = LocalDateTime.now()
    println("""
        
           ______                     __ 
          / ____/___  ____ ___  ___  / /_
         / /   / __ \/ __ `__ \/ _ \/ __/
        / /___/ /_/ / / / / / /  __/ /_  
        \____/\____/_/ /_/ /_/\___/\__/  


    """.trimIndent()
    )
    FileUtil.initLog()
    DataSetup.initData()
    val qqId = BotVariables.cfg.botId
    val password = BotVariables.cfg.botPassword

    if (qqId == 0L) {
        println("请到 config.json 里填写机器人的QQ号&密码")
        exitProcess(0)
    } else {
        val config = BotConfiguration.Default
        config.botLoggerSupplier = { it ->
            PlatformLogger("Bot ${it.id}", {
                Comet.log.writeString(Comet.log.getContext() + "$it\n")
                println(it)
            })
        }
        config.networkLoggerSupplier = { it ->
            PlatformLogger("Net ${it.id}", {
                Comet.log.writeString(Comet.log.getContext() + "$it\n")
                println(it)
            })
        }
        config.heartbeatPeriodMillis = BotVariables.cfg.heartBeatPeriod * 60 * 1000
        config.fileBasedDeviceInfo()
        bot = Bot(qq = qqId, password = password, configuration = config)
        bot.alsoLogin()
        Comet.logger = bot.logger

        Comet.setupRCon()

        Comet.service = Executors.newScheduledThreadPool(
                4,
                BasicThreadFactory.Builder().namingPattern("bot-service-%d").daemon(true).build()
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
                        TwitterCommand(),
                        VersionCommand(),
                        // Console Command
                        StopCommand(),
                        DebugCommand()
                )
        )

        Comet.logger.info("[命令] 已注册 " + MessageHandler.countCommands() + " 个命令")

        /** 监听器 */
        val listeners = arrayOf(ConvertLightAppListener, RepeatListener)

        listeners.forEach {
            it.register(bot)
            Comet.logger.info("[监听器] 已注册 ${it.getName()} 监听器")
        }

        startUpTask()

        val time = Duration.between(startTime, LocalDateTime.now())
        val startUsedTime = "${time.toSecondsPart()}s${time.toMillisPart()}ms"

        Comet.logger.info("彗星 Bot 启动成功, 耗时 $startUsedTime")

        Runtime.getRuntime().addShutdownHook(Thread {
            Comet.logger.info("[Bot] 正在关闭 Bot...")
            DataSetup.saveFiles()
            Comet.service.shutdown()
            Comet.rCon?.disconnect()
        })

        bot.subscribeMessages {
            always {
                if (BotVariables.switch && sender.id != 80000000L) {
                    val result = MessageHandler.execute(this)
                    if (result !is EmptyMessageChain) {
                        reply(result)
                    }
                }
            }
        }

        Comet.executeCommand()

        bot.join() // 等待 Bot 离线, 避免主线程退出
    }
}

fun startUpTask() {
    val apis = arrayOf(BiliBiliApi, TwitterApi)

    /** 定时任务 */
    BackupHelper.scheduleBackup()
    TaskManager.runScheduleTaskAsync(
            { BotVariables.users.forEach { it.addTime(100) } },
            5,
            5,
            TimeUnit.HOURS
    )

    TaskManager.runScheduleTaskAsyncIf(
            BiliBiliLiveStatusChecker::run,
            BotVariables.cfg.checkDelay,
            BotVariables.cfg.checkDelay,
            TimeUnit.MINUTES,
            BotVariables.cfg.subList.isNotEmpty()
    )

    TaskManager.runAsync({
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

    TaskManager.runScheduleTaskAsync({ apis.forEach { it.resetTime() } }, 25, 25, TimeUnit.MINUTES)
    TaskManager.runScheduleTaskAsyncIf(
            TweetUpdateChecker::run,
            1,
            8,
            TimeUnit.MINUTES,
            (BotVariables.cfg.twitterSubs.isNotEmpty() && BotVariables.cfg.tweetPushGroups.isNotEmpty())
    )

    TaskManager.runScheduleTaskAsync(HitokotoUpdater::run, 5, 60 * 60 * 24, TimeUnit.SECONDS)
}

