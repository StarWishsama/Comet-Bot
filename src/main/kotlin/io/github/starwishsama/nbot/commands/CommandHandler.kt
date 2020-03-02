package io.github.starwishsama.nbot.commands

import io.github.starwishsama.nbot.BotConstants
import io.github.starwishsama.nbot.commands.interfaces.GroupCommand
import io.github.starwishsama.nbot.commands.subcommands.BotCommand
import io.github.starwishsama.nbot.commands.subcommands.DebugCommand
import net.mamoe.mirai.message.FriendMessage
import net.mamoe.mirai.message.GroupMessage
import net.mamoe.mirai.message.data.MessageChain

class CommandHandler {
    var commands: List<GroupCommand> = mutableListOf()

    fun setupCommand(command: GroupCommand){
        this.commands = this.commands + command
    }

    fun setupCommand(commands: Array<GroupCommand>){
        this.commands = this.commands + commands
    }

    suspend fun execute(raw: GroupMessage): MessageChain? {
        val cmdPrefix: String
        var temp = ""
        for (s : String in BotConstants().cfg.commandPrefix){
            temp = raw.message.toString().replace(s, "")
        }

        cmdPrefix = temp.split(" ")[0].substring(1)

        for (cmd : GroupCommand in commands){
            println(cmd.getProps().name)
            if (cmd.getProps().name == cmdPrefix){
                return cmd.executeGroup(raw)
            }
        }
        return null;
    }

    suspend fun execute(raw: FriendMessage): MessageChain?{
        return null;
    }

    fun getInstance(): CommandHandler {
        return this;
    }
}