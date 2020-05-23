package io.github.starwishsama.nbot

import com.hiczp.bilibili.api.BilibiliClient
import io.github.starwishsama.nbot.BotMain.bot
import io.github.starwishsama.nbot.BotMain.client
import io.github.starwishsama.nbot.BotMain.executeCommand
import io.github.starwishsama.nbot.BotMain.initLog
import io.github.starwishsama.nbot.BotMain.log
import io.github.starwishsama.nbot.BotMain.rCon
import io.github.starwishsama.nbot.BotMain.setupRCon
import io.github.starwishsama.nbot.api.bilibili.DynamicApi
import io.github.starwishsama.nbot.commands.CommandExecutor
import io.github.starwishsama.nbot.commands.subcommands.*
import io.github.starwishsama.nbot.enums.UserLevel
import io.github.starwishsama.nbot.file.BackupHelper
import io.github.starwishsama.nbot.file.DataSetup
import io.github.starwishsama.nbot.listeners.FuckLightAppListener
import io.github.starwishsama.nbot.listeners.GroupChatListener
import io.github.starwishsama.nbot.listeners.RepeatListener
import io.github.starwishsama.nbot.listeners.SessionListener
import io.github.starwishsama.nbot.managers.TaskManager
import io.github.starwishsama.nbot.objects.BotUser
import io.github.starwishsama.nbot.tasks.CheckLiveStatus
import io.github.starwishsama.nbot.api.twitter.TwitterApi
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
    const val version = "0.3.5-BETA-200523"
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
                logger.info("[Bot] 正在关闭 Bot...")
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
    initLog()
    BotMain.startTime = System.currentTimeMillis()
    DataSetup.initData()
    BotMain.qqId = BotConstants.cfg.botId
    BotMain.password = BotConstants.cfg.botPassword

    if (BotMain.qqId == 0L) {
        println("请到 config.json 里填写机器人的QQ号&密码")
        BotMain.logger.info("[Bot] Stopping bot...")
        exitProcess(0)
    } else {
        val config = BotConfiguration.Default
        config.botLoggerSupplier = { it -> PlatformLogger("Bot ${it.id}", {
            log.writeString(log.getContext() + "$it\n")
            println(it)
        })}
        config.networkLoggerSupplier = { it -> PlatformLogger("Net ${it.id}", {
            log.writeString(log.getContext() + "$it\n")
            println(it)
        })}
        config.fileBasedDeviceInfo()
        bot = Bot(qq = BotMain.qqId, password = BotMain.password, configuration = config)
        bot.alsoLogin()
        BotMain.logger = bot.logger
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

        setupRCon()

        BotMain.service = Executors.newSingleThreadScheduledExecutor(
                BasicThreadFactory.Builder().namingPattern("bot-service-%d").daemon(true).build()
        )

        /** 服务 */
        BackupHelper.scheduleBackup()
        TaskManager.runScheduleTaskAsync(
            {BotConstants.users.forEach {it.addTime(100)}},
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
            client.runCatching {
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
        TaskManager.runScheduleTaskAsync({ apis.forEach{ it.resetTime() }}, 25, 25, TimeUnit.MINUTES)

        /** 监听器 */
        listeners.forEach {
            it.register(bot)
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
            DataSetup.saveFiles()
            BotMain.service.shutdown()
            rCon?.disconnect()
        })

        bot.subscribeMessages {
            always {
                if (sender.id != 80000000L) {
                    val result = CommandExecutor.execute(this)
                    if (result !is EmptyMessageChain) {
                        reply(result)
                    }
                }
            }
        }

        executeCommand()

        bot.join() // 等待 Bot 离线, 避免主线程退出
    }
}

