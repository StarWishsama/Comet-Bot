package io.github.starwishsama.nbot.commands.subcommands

import io.github.starwishsama.nbot.BotInstance
import io.github.starwishsama.nbot.commands.CommandHandler
import io.github.starwishsama.nbot.commands.CommandProps
import io.github.starwishsama.nbot.commands.interfaces.UniversalCommand
import io.github.starwishsama.nbot.enums.UserLevel
import io.github.starwishsama.nbot.objects.BotUser
import io.github.starwishsama.nbot.util.BotUtil
import net.mamoe.mirai.message.ContactMessage
import net.mamoe.mirai.message.data.*

class BotCommand : UniversalCommand {
    override suspend fun execute(message: ContactMessage, args: List<String>, user: BotUser): MessageChain {
        if (BotUtil.isNoCoolDown(message.sender.id)) {
            val v = "无名Bot " + BotInstance.version + "\n已注册的命令个数: " + CommandHandler.commands.size + "\n运行时间: ${BotUtil.getRunningTime()}"
            return v.toMessage().asMessageChain()
        }
        return EmptyMessageChain
    }

    override fun getProps(): CommandProps {
        return CommandProps("bot", arrayListOf("version", "v", "版本"), "nbot.commands.version", UserLevel.USER)
    }

    override fun getHelp(): String = ""
}