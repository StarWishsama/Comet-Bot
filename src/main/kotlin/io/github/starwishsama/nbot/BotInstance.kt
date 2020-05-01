package io.github.starwishsama.nbot

import com.hiczp.bilibili.api.BilibiliClient
import io.github.starwishsama.nbot.BotInstance.Companion.logger
import io.github.starwishsama.nbot.commands.CommandHandler
import io.github.starwishsama.nbot.commands.subcommands.*
import io.github.starwishsama.nbot.config.BackupHelper
import io.github.starwishsama.nbot.config.DataSetup
import io.github.starwishsama.nbot.enums.UserLevel
import io.github.starwishsama.nbot.listeners.GroupChatListener
import io.github.starwishsama.nbot.listeners.RepeatListener
import io.github.starwishsama.nbot.listeners.SessionListener
import io.github.starwishsama.nbot.objects.BotUser
import net.mamoe.mirai.Bot
import net.mamoe.mirai.alsoLogin
import net.mamoe.mirai.event.subscribeMessages
import net.mamoe.mirai.join
import net.mamoe.mirai.message.data.EmptyMessageChain
import net.mamoe.mirai.utils.MiraiLogger
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.concurrent.BasicThreadFactory
import java.io.File
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import kotlin.system.exitProcess

class BotInstance private constructor() {
    companion object {
        val filePath: File? = File(getPath())
        const val version = "0.1.2-Canary-200501"
        private var qqId = 0L
        private lateinit var password: String
        lateinit var bot : Bot
        var handler = CommandHandler().getInstance()
        val client = BilibiliClient()
        var startTime: Long = 0
        lateinit var service : ScheduledExecutorService
        lateinit var logger: MiraiLogger
        val instance: BotInstance
            @Synchronized
            get() { return BotInstance() }
    }

    suspend fun run() {
        startTime = System.currentTimeMillis()
        DataSetup.loadCfg()
        DataSetup.loadLang()
        qqId = BotConstants.cfg.botId
        password = BotConstants.cfg.botPassword
        bot = Bot(qqId, password)
        bot.alsoLogin()
        logger = bot.logger
        handler.setupCommand(
            arrayOf(
                AdminCommand(),
                BotCommand(),
                BiliBiliCommand(),
                DrawCommand(),
                DebugCommand(),
                FlowerCommand(),
                PictureSearch(),
                MuteCommand(),
                MusicCommand(),
                R6SCommand(),
                CheckInCommand(),
                ClockInCommand(),
                InfoCommand(),
                DivineCommand()
            )
        )
        logger.info("已注册 " + CommandHandler.commands.size + " 个命令")

        if (!BotConstants.cfg.biliUserName.isNullOrBlank() && BotConstants.cfg.biliPassword.isNullOrBlank()) {
            client.runCatching {
                BotConstants.cfg.biliPassword?.let {
                    login(username = BotConstants.cfg.biliUserName!!, password = it)
                }
            }
        }

        service = Executors.newSingleThreadScheduledExecutor(
            BasicThreadFactory.Builder().namingPattern("bot-service-%d").daemon(true).build()
        )

        /** 备份服务 */
        service.scheduleAtFixedRate({ BackupHelper.createBackup() }, 0, 3, TimeUnit.HOURS)
        service.scheduleAtFixedRate({ BotConstants.users.forEach { it.addTime(100) } }, 1, 1, TimeUnit.HOURS)

        /** 监听器 */
        SessionListener.register(bot)
        RepeatListener.register(bot)
        GroupChatListener.register(bot)

        val startUsedTime = if (System.currentTimeMillis() - startTime > 1000) {
            String.format("%.2f", ((System.currentTimeMillis() - startTime.toDouble()) / 1000)) + "s"
        } else {
            ((System.currentTimeMillis() - startTime).toString() + "ms")
        }

        logger.info("无名 Bot 启动成功, 耗时 $startUsedTime")

        bot.subscribeMessages {
            always {
                if (this.message.contentToString().isNotEmpty() && BotConstants.cfg.commandPrefix.contains(
                        this.message.contentToString().substring(0, 1)
                    )
                ) {
                    val result = handler.execute(this)
                    if (result !is EmptyMessageChain) {
                        reply(result)
                    }
                }
            }
        }

        Runtime.getRuntime().addShutdownHook(Thread(Runnable {
            DataSetup.saveFiles()
            service.shutdown()
        }))

        executeCommand()

        bot.join() // 等待 Bot 离线, 避免主线程退出
    }
}

suspend fun main(){
    BotInstance.instance.run()
}

private fun executeCommand(){
    val scanner = Scanner(System.`in`)
    var command: String
    while (scanner.hasNextLine()) {
        command = scanner.nextLine()
        if ("stop" == command) {
            logger.info("Stopping bot...")
            exitProcess(0)
        } else if ("upgrade" == command){
            val cmd = command.split(" ")
            if (cmd.isNotEmpty() && StringUtils.isNumeric(cmd[1])){
                val user = BotUser.getUser(cmd[1].toLong())
                if (user != null){
                    logger.info("已升级权限组至 ${UserLevel.upgrade(user)}")
                } else {
                    logger.info("找不到此用户")
                }
            } else {
                logger.warning("请输入有效的QQ号")
            }
        }
    }
    scanner.close()
}

private fun getPath(): String {
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

