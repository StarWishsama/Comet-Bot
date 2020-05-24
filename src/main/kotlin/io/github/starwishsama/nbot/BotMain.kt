package io.github.starwishsama.nbot

import com.hiczp.bilibili.api.BilibiliClient
import io.github.starwishsama.nbot.api.bilibili.DynamicApi
import io.github.starwishsama.nbot.commands.CommandExecutor
import io.github.starwishsama.nbot.commands.subcommands.*
import io.github.starwishsama.nbot.enums.UserLevel
import io.github.starwishsama.nbot.file.*
import io.github.starwishsama.nbot.listeners.*
import io.github.starwishsama.nbot.managers.TaskManager
import io.github.starwishsama.nbot.objects.BotUser
import io.github.starwishsama.nbot.tasks.CheckLiveStatus
import io.github.starwishsama.nbot.api.twitter.TwitterApi
import io.github.starwishsama.nbot.tasks.LatestTweetChecker
import io.github.starwishsama.nbot.util.getContext
import io.github.starwishsama.nbot.util.writeString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import net.kronos.rkon.core.Rcon
import net.mamoe.mirai.Bot
import net.mamoe.mirai.alsoLogin
import net.mamoe.mirai.event.subscribeMessages
import net.mamoe.mirai.join
import net.mamoe.mirai.message.data.EmptyMessageChain
import net.mamoe.mirai.utils.*
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.concurrent.BasicThreadFactory
import java.io.File
import java.io.IOException
import java.time.LocalDateTime
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import kotlin.system.exitProcess

object BotMain {
    val filePath: File = File(getPath())
    const val version = "0.3.6-rc1-200524"
    var qqId = 0L
    lateinit var password: String
    lateinit var bot: Bot
    val client = BilibiliClient()
    var startTime: Long = 0
    lateinit var service: ScheduledExecutorService
    lateinit var logger: MiraiLogger
    var rCon: Rcon? = null
    lateinit var log: File

    fun executeCommand() {
        val scanner = Scanner(System.`in`)
        var command: String
        while (scanner.hasNextLine()) {
            command = scanner.nextLine()
            if ("stop" == command) {
                exitProcess(0)
            } else if ("upgrade" == command) {
                val cmd = command.split(" ")
                if (cmd.isNotEmpty() && StringUtils.isNumeric(cmd[1])) {
                    val user = BotUser.getUser(cmd[1].toLong())
                    if (user != null) {
                        logger.info("[后台命令] 已升级权限组至 ${UserLevel.upgrade(user)}")
                    } else {
                        logger.info("[后台命令] 找不到此用户")
                    }
                } else {
                    logger.warning("[后台命令] 请输入有效的QQ号")
                }
            }
        }
        scanner.close()
    }

    private fun getPath(): String {
        var path: String = BotMain::class.java.protectionDomain.codeSource.location.path
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
        if (BotConstants.cfg.rConUrl != null && BotConstants.cfg.rConPassword != null && rCon == null) {
            rCon = Rcon(BotConstants.cfg.rConUrl!!, BotConstants.cfg.rConPort, BotConstants.cfg.rConPassword!!.toByteArray())
        }
    }

    fun initLog() {
        try {
            val initTime = LocalDateTime.now()
            val parent = File(getPath() + File.separator + "logs")
            if (!parent.exists()) {
                parent.mkdirs()
            }
            log = File(parent, "log-${initTime.year}-${initTime.month.value}-${initTime.dayOfMonth}-${initTime.hour}-${initTime.minute}.log")
            log.createNewFile()
        } catch (e: IOException) {
            error("尝试输出 Log 失败")
        }
    }
}

suspend fun main() {
    BotMain.initLog()
    BotMain.startTime = System.currentTimeMillis()
    DataSetup.initData()
    BotMain.qqId = BotConstants.cfg.botId
    BotMain.password = BotConstants.cfg.botPassword

    if (BotMain.qqId == 0L) {
        println("请到 config.json 里填写机器人的QQ号&密码")
        exitProcess(0)
    } else {
        val config = BotConfiguration.Default
        config.botLoggerSupplier = { it ->
            PlatformLogger("Bot ${it.id}", {
                BotMain.log.writeString(BotMain.log.getContext() + "$it\n")
                println(it)
            })
        }
        config.networkLoggerSupplier = { it ->
            PlatformLogger("Net ${it.id}", {
                BotMain.log.writeString(BotMain.log.getContext() + "$it\n")
                println(it)
            })
        }
        config.heartbeatPeriodMillis = BotConstants.cfg.heartBeatPeriod * 60 * 1000
        config.fileBasedDeviceInfo()
        BotMain.bot = Bot(qq = BotMain.qqId, password = BotMain.password, configuration = config)
        BotMain.bot.alsoLogin()
        BotMain.logger = BotMain.bot.logger
        CommandExecutor.setupCommand(
                arrayOf(
                        AdminCommand(),
                        BiliBiliCommand(),
                        CheckInCommand(),
                        ClockInCommand(),
                        DebugCommand(),
                        DivineCommand(),
                        GachaCommand(),
                        GuessNumberCommand(),
                        FlowerCommand(),
                        HelpCommand(),
                        InfoCommand(),
                        MusicCommand(),
                        MuteCommand(),
                        PictureSearch(),
                        R6SCommand(),
                        RConCommand(),
                        TwitterCommand(),
                        VersionCommand()
                )
        )

        val listeners = arrayOf(FuckLightAppListener, GroupChatListener, RepeatListener, SessionListener)
        val apis = arrayOf(DynamicApi, TwitterApi)

        BotMain.logger.info("[命令] 已注册 " + CommandExecutor.commands.size + " 个命令")

        BotMain.setupRCon()

        BotMain.service = Executors.newSingleThreadScheduledExecutor(
                BasicThreadFactory.Builder().namingPattern("bot-service-%d").daemon(true).build()
        )

        /** 服务 */
        BackupHelper.scheduleBackup()
        TaskManager.runScheduleTaskAsync(
                { BotConstants.users.forEach { it.addTime(100) } },
                5,
                5,
                TimeUnit.HOURS)
        TaskManager.runScheduleTaskAsyncIf(
                CheckLiveStatus::run,
                BotConstants.cfg.checkDelay,
                BotConstants.cfg.checkDelay,
                TimeUnit.MINUTES,
                BotConstants.cfg.subList.isNotEmpty()
        )
        TaskManager.runAsync({
            BotMain.client.runCatching {
                val pwd = BotConstants.cfg.biliPassword
                val uname = BotConstants.cfg.biliUserName

                if (pwd != null && uname != null) {
                    runBlocking {
                        withContext(Dispatchers.IO) {
                            login(username = uname, password = pwd)
                        }
                    }
                }
            }

        }, 5)
        TaskManager.runScheduleTaskAsync({ apis.forEach { it.resetTime() } }, 25, 25, TimeUnit.MINUTES)
        TaskManager.runScheduleTaskAsyncIf(
                LatestTweetChecker::run,
                1,
                15,
                TimeUnit.MINUTES,
                (BotConstants.cfg.twitterSubs.isNotEmpty() && BotConstants.cfg.tweetPushGroups.isNotEmpty())
        )

        /** 监听器 */
        listeners.forEach {
            it.register(BotMain.bot)
            BotMain.logger.info("[监听器] 已注册 ${it.getName()} 监听器")
        }

        val time = System.currentTimeMillis() - BotMain.startTime
        val startUsedTime =
                if (time > 1000) {
                    String.format("%.2f", (time.toDouble() / 1000)) + "s"
                } else {
                    (time.toString() + "ms")
                }

        BotMain.logger.info("无名 Bot 启动成功, 耗时 $startUsedTime")

        Runtime.getRuntime().addShutdownHook(Thread {
            BotMain.logger.info("[Bot] 正在关闭 Bot...")
            DataSetup.saveFiles()
            BotMain.service.shutdown()
            BotMain.rCon?.disconnect()
        })

        BotMain.bot.subscribeMessages {
            always {
                if (sender.id != 80000000L) {
                    val result = CommandExecutor.execute(this)
                    if (result !is EmptyMessageChain) {
                        reply(result)
                    }
                }
            }
        }

        BotMain.executeCommand()

        BotMain.bot.join() // 等待 Bot 离线, 避免主线程退出
    }
}

