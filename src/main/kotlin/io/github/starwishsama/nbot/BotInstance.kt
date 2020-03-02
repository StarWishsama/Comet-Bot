@file:Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")

package io.github.starwishsama.nbot

import com.hiczp.bilibili.api.BilibiliClient
import io.github.starwishsama.nbot.commands.CommandHandler
import io.github.starwishsama.nbot.commands.subcommands.BotCommand
import io.github.starwishsama.nbot.commands.subcommands.DebugCommand
import io.github.starwishsama.nbot.util.FileSetup
import net.mamoe.mirai.Bot
import net.mamoe.mirai.alsoLogin
import net.mamoe.mirai.event.subscribeGroupMessages

import java.io.File

class BotInstance {
    val filePath: File? = File(getPath())
    val version = "0.0.1-ALPHA-Mirai"
    val constants = BotConstants()
    private val qqId = constants.cfg.botId
    private val password = constants.cfg.botPassword
    val bot = Bot(qqId, password)
    var handler = CommandHandler().getInstance()
    val client = BilibiliClient()
    val logger = bot.logger

    suspend fun run() {
        FileSetup.loadCfg(constants)
        FileSetup.loadLang(constants)

        bot.alsoLogin()

        handler.setupCommand(arrayOf(BotCommand(), DebugCommand()))
        bot.logger.info("已注册 " + handler.commands.size + " 个命令")

        if (!constants.cfg.biliUserName.isNullOrBlank() && constants.cfg.biliPassword.isNullOrBlank()) {
            BotInstance().client.runCatching {
                constants.cfg.biliUserName?.let {
                    login(constants.cfg.biliPassword!!, it)
                }
            }
        }

        bot.subscribeGroupMessages {
            always {
                if (BotConstants().cfg.commandPrefix.contains(this.message.toString().substring(0, 1))) {
                    val result = handler.execute(this)
                    if (result != null) {
                        reply(result)
                    }
                }
            }
        }

        bot.join() // 等待 Bot 离线, 避免主线程退出
    }
}

suspend fun main(){
    BotInstance().run()
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

