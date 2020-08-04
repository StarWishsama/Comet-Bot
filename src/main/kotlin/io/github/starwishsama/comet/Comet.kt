package io.github.starwishsama.comet

import io.github.starwishsama.comet.BotVariables.bot
import io.github.starwishsama.comet.BotVariables.startTime
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
import net.mamoe.mirai.contact.isBotMuted
import net.mamoe.mirai.event.subscribeMessages
import net.mamoe.mirai.join
import net.mamoe.mirai.message.GroupMessageEvent
import net.mamoe.mirai.message.data.EmptyMessageChain
import net.mamoe.mirai.utils.BotConfiguration
import net.mamoe.mirai.utils.PlatformLogger
import java.time.Duration
import java.time.LocalDateTime
import java.util.*
import java.util.concurrent.TimeUnit
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

    private fun handleConsoleCommand() {
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

    private fun startAllPusher() {
        val pushers = arrayOf(BiliLiveChecker, TweetUpdateChecker)
        pushers.forEach {
            val future = TaskUtil.runScheduleTaskAsync(it::retrieve, it.delayTime, it.cycle, TimeUnit.MINUTES)
            it.future = future
        }
    }

    @ExperimentalTime
    suspend fun startBot(qqId: Long, password: String) {
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
        bot = Bot(qq = qqId, password = password, configuration = config)
        bot.alsoLogin()
        BotVariables.logger = bot.logger

        DataSetup.initPerGroupSetting()

        setupRCon()

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
                        GroupConfigCommand(),
                        // Console Command
                        StopCommand(),
                        DebugCommand(),
                        io.github.starwishsama.comet.commands.subcommands.console.AdminCommand()
                )
        )

        BotVariables.logger.info("[命令] 已注册 " + MessageHandler.countCommands() + " 个命令")

        /** 监听器 */
        val listeners = arrayOf(ConvertLightAppListener, RepeatListener)

        listeners.forEach {
            it.register(bot)
            BotVariables.logger.info("[监听器] 已注册 ${it.getName()} 监听器")
        }

        startUpTask()
        startAllPusher()

        val duration = Duration.between(startTime, LocalDateTime.now())

        BotVariables.logger.info("彗星 Bot 启动成功, 耗时 ${duration.toKotlinDuration().toFriendly()}")

        Runtime.getRuntime().addShutdownHook(Thread {
            BotVariables.logger.info("[Bot] 正在关闭 Bot...")
            DataSetup.saveFiles()
            BotVariables.service.shutdown()
            BotVariables.rCon?.disconnect()
        })

        bot.subscribeMessages {
            always {
                if (sender.id != 80000000L) {
                    if (this is GroupMessageEvent && group.isBotMuted) return@always

                    val result = MessageHandler.execute(this)
                    try {
                        if (result.msg !is EmptyMessageChain && result.msg.isNotEmpty()) {
                            reply(result.msg)
                        }
                    } catch (e: IllegalArgumentException) {
                        BotVariables.logger.warning("正在尝试发送空消息, 执行结果 $result")
                    }
                }
            }
        }

        handleConsoleCommand()

        bot.join() // 等待 Bot 离线, 避免主线程退出
    }
}

@ExperimentalStdlibApi
@ExperimentalTime
suspend fun main() {
    startTime = LocalDateTime.now()
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

    val id = BotVariables.cfg.botId

    if (id == 0L) {
        println("请输入欲登录的机器人账号")
        val scanner = Scanner(System.`in`)
        var command: String
        while (scanner.hasNextLine()) {
            command = scanner.nextLine()
            if (BotVariables.cfg.botId == 0L && command.isNumeric()) {
                BotVariables.cfg.botId = command.toLong()
                println("成功设置账号为 ${BotVariables.cfg.botId}")
                println("请输入欲登录的机器人密码")
            } else if (BotVariables.cfg.botPassword.isEmpty()) {
                BotVariables.cfg.botPassword = command
                println("成功设置密码, 按下 Enter 启动机器人")
            } else if (BotVariables.cfg.botId != 0L && BotVariables.cfg.botPassword.isNotEmpty()) {
                println("请稍等...")
                Comet.startBot(BotVariables.cfg.botId, BotVariables.cfg.botPassword)
                break
            }
        }
        scanner.close()
    } else {
        Comet.startBot(BotVariables.cfg.botId, BotVariables.cfg.botPassword)
    }
}
