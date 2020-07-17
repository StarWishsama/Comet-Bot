package io.github.starwishsama.comet

import com.hiczp.bilibili.api.BilibiliClient
import io.github.starwishsama.comet.Comet.bot
import io.github.starwishsama.comet.Comet.startTime
import io.github.starwishsama.comet.api.bilibili.BiliBiliApi
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
import io.github.starwishsama.comet.tasks.CheckLiveStatus
import io.github.starwishsama.comet.tasks.HitokotoUpdater
import io.github.starwishsama.comet.tasks.LatestTweetChecker
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
import java.io.IOException
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import kotlin.system.exitProcess

object Comet {
    val filePath: File = File(getPath())
    const val version = "0.3.8.1-DEV-1abbd40-20200717"
    var qqId = 0L
    lateinit var password: String
    lateinit var bot: Bot
    val client = BilibiliClient()
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

    private fun getPath(): String {
        var path: String = Comet::class.java.protectionDomain.codeSource.location.path
        if (System.getProperty("os.name").toLowerCase().contains("dows")) {
            path = path.substring(1)
        }
        if (path.contains("jar")) {
            path = path.substring(0, path.lastIndexOf("/"))
            return path
        }
        val location = File(path.replace("target/classes/", ""))
        return location.path
    }

    fun setupRCon() {
        val url = BotVariables.cfg.rConUrl
        val pwd = BotVariables.cfg.rConPassword
        if (url != null && pwd != null && rCon == null) {
            rCon = Rcon(url, BotVariables.cfg.rConPort, pwd.toByteArray())
        }
    }

    fun initLog() {
        try {
            val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss")
            val initTime = LocalDateTime.now()
            val parent = File(getPath() + File.separator + "logs")
            if (!parent.exists()) {
                parent.mkdirs()
            }
            log = File(parent, "log-${dateFormatter.format(initTime)}.log")
            log.createNewFile()
        } catch (e: IOException) {
            error("尝试输出 Log 失败")
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
    Comet.initLog()
    DataSetup.initData()
    Comet.qqId = BotVariables.cfg.botId
    Comet.password = BotVariables.cfg.botPassword

    if (Comet.qqId == 0L) {
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
        bot = Bot(qq = Comet.qqId, password = Comet.password, configuration = config)
        bot.alsoLogin()
        Comet.logger = bot.logger
        MessageHandler.setupCommand(
            arrayOf(
                AdminCommand(),
                BiliBiliCommand(),
                CheckInCommand(),
                ClockInCommand(),
                DebugCommand(),
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

        val listeners = arrayOf(ConvertLightAppListener, RepeatListener)
        val apis = arrayOf(BiliBiliApi, TwitterApi)

        Comet.logger.info("[命令] 已注册 " + MessageHandler.commands.size + " 个命令")

        Comet.setupRCon()

        Comet.service = Executors.newScheduledThreadPool(
                4,
                BasicThreadFactory.Builder().namingPattern("bot-service-%d").daemon(true).build()
        )

        /** 定时任务 */
        BackupHelper.scheduleBackup()
        TaskManager.runScheduleTaskAsync(
            { BotVariables.users.forEach { it.addTime(100) } },
            5,
            5,
            TimeUnit.HOURS
        )
        TaskManager.runScheduleTaskAsyncIf(
            CheckLiveStatus::run,
            BotVariables.cfg.checkDelay,
            BotVariables.cfg.checkDelay,
            TimeUnit.MINUTES,
            BotVariables.cfg.subList.isNotEmpty()
        )
        TaskManager.runAsync({
            Comet.client.runCatching {
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
            LatestTweetChecker::run,
            1,
            8,
            TimeUnit.MINUTES,
            (BotVariables.cfg.twitterSubs.isNotEmpty() && BotVariables.cfg.tweetPushGroups.isNotEmpty())
        )
        TaskManager.runScheduleTaskAsync(HitokotoUpdater::run, 5, 60 * 60 * 24, TimeUnit.SECONDS)

        /** 监听器 */
        listeners.forEach {
            it.register(bot)
            Comet.logger.info("[监听器] 已注册 ${it.getName()} 监听器")
        }

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
                if (sender.id != 80000000L) {
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

