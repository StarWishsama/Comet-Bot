package io.github.starwishsama.nbot.commands.subcommands

import io.github.starwishsama.nbot.BotInstance
import io.github.starwishsama.nbot.commands.CommandHandler
import io.github.starwishsama.nbot.commands.CommandProps
import io.github.starwishsama.nbot.commands.interfaces.GroupCommand
import io.github.starwishsama.nbot.enums.UserLevel
import net.mamoe.mirai.message.GroupMessage
import net.mamoe.mirai.message.data.*

class BotCommand : GroupCommand {
    override suspend fun executeGroup(message: GroupMessage): MessageChain {
        val v = "无名Bot " + BotInstance().version + "\n已注册的命令个数: " + BotInstance().handler.commands.size
        return v.toMessage().toChain()
    }

    override fun getProps(): CommandProps {
        return CommandProps("bot", arrayListOf("version", "v", "版本"), "nbot.commands.version", UserLevel.USER)
    }
}