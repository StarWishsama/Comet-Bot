package io.github.starwishsama.comet.commands.subcommands.chats

import io.github.starwishsama.comet.BotMain
import io.github.starwishsama.comet.commands.CommandExecutor
import io.github.starwishsama.comet.commands.CommandProps
import io.github.starwishsama.comet.commands.interfaces.UniversalCommand
import io.github.starwishsama.comet.enums.UserLevel
import io.github.starwishsama.comet.objects.BotUser
import io.github.starwishsama.comet.utils.BotUtil
import io.github.starwishsama.comet.utils.toMirai
import net.mamoe.mirai.message.MessageEvent
import net.mamoe.mirai.message.data.EmptyMessageChain
import net.mamoe.mirai.message.data.MessageChain

class VersionCommand : UniversalCommand {
    override suspend fun execute(event: MessageEvent, args: List<String>, user: BotUser): MessageChain {
        if (BotUtil.isNoCoolDown(event.sender.id)) {
            return ("无名Bot " + BotMain.version + "\n已注册的命令个数: " + CommandExecutor.commands.size +
                    "\n运行时间: ${BotUtil.getRunningTime()}" +
                    "\nMade with ❤, Running on Mirai").toMirai()
        }
        return EmptyMessageChain
    }

    override fun getProps(): CommandProps {
        return CommandProps("version", arrayListOf("v", "版本"), "查看版本号", "nbot.commands.version", UserLevel.USER)
    }

    override fun getHelp(): String = ""
}