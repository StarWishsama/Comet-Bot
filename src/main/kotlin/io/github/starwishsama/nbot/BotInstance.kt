package io.github.starwishsama.nbot

import com.hiczp.bilibili.api.BilibiliClient
import io.github.starwishsama.nbot.BotInstance.bot
import io.github.starwishsama.nbot.BotInstance.client
import io.github.starwishsama.nbot.BotInstance.executeCommand
import io.github.starwishsama.nbot.BotInstance.initLog
import io.github.starwishsama.nbot.BotInstance.log
import io.github.starwishsama.nbot.BotInstance.rCon
import io.github.starwishsama.nbot.BotInstance.setupRCon
import io.github.starwishsama.nbot.commands.CommandHandler
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
import io.github.starwishsama.nbot.util.getContext
import io.github.starwishsama.nbot.util.writeString
import io.github.starwishsama.nbot.util.TwitterUtil
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

object BotInstance {
    val filePath: File = File(getPath())
    const val version = "0.3.2-BETA-200509"
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
                logger.info("[Bot] Stopping bot...")
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

    fun getPath(): String {
        var path: String = BotInstance::class.java.protectionDomain.codeSource.location.path
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
            log = File(parent, "log-${initTime.year}-${initTime.month.value}-${initTime.dayOfMonth}-${initTime.hour}-${initTime.minute}.txt")
            log.createNewFile()
        } catch (e: IOException) {
            error("尝试输出 Log 失败")
        }
    }
}

suspend fun main() {
    initLog()
    BotInstance.startTime = System.currentTimeMillis()
    DataSetup.initData()
    BotInstance.qqId = BotConstants.cfg.botId
    BotInstance.password = BotConstants.cfg.botPassword

    if (BotInstance.qqId == 0L) {
        println("请到 config.json 里填写机器人的QQ号&密码")
        BotInstance.logger.info("[Bot] Stopping bot...")
        exitProcess(0)
    } else {
        val config = BotConfiguration.Default
        config.botLoggerSupplier = { it ->
            PlatformLogger("Bot(${it.id})") {
                log.writeString(log.getContext() + "$it\n")
                println(it)
            }
        }
        config.networkLoggerSupplier = { it ->
            PlatformLogger("Network(${it.bot.id})") {
                log.writeString(log.getContext() + "$it\n")
                println(it)
            }
        }
        bot = Bot(qq = BotInstance.qqId, password = BotInstance.password, configuration = config)
        bot.alsoLogin()
        BotInstance.logger = bot.logger
        CommandHandler.setupCommand(
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

        BotInstance.logger.info("[命令] 已注册 " + CommandHandler.commands.size + " 个命令")

        setupRCon()

        BotInstance.service = Executors.newSingleThreadScheduledExecutor(
                BasicThreadFactory.Builder().namingPattern("bot-service-%d").daemon(true).build()
        )

        /** 服务 */
        BackupHelper.scheduleBackup()
        TaskManager.runScheduleTaskAsync({ BotConstants.users.forEach { it.addTime(100) } }, 5, 5, TimeUnit.HOURS)
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
        TaskManager.runScheduleTaskAsync({ TwitterUtil.apiExecuteTime = 0 }, 15, 15, TimeUnit.MINUTES)

        /** 监听器 */
        listeners.forEach {
            it.register(bot)
            BotInstance.logger.info("[监听器] 已注册 ${it.getName()} 监听器")
        }

        val time = System.currentTimeMillis() - BotInstance.startTime
        val startUsedTime =
                if (time > 1000) {
                    String.format("%.2f", (time.toDouble() / 1000)) + "s"
                } else {
                    (time.toString() + "ms")
                }

        BotInstance.logger.info("无名 Bot 启动成功, 耗时 $startUsedTime")

        Runtime.getRuntime().addShutdownHook(Thread {
            DataSetup.saveFiles()
            BotInstance.service.shutdown()
            rCon?.disconnect()
        })

        bot.subscribeMessages {
            always {
                if (sender.id != 80000000L) {
                    val result = CommandHandler.execute(this)
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

