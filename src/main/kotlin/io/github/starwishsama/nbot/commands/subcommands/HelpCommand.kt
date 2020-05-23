package io.github.starwishsama.nbot.commands.subcommands

import io.github.starwishsama.nbot.commands.CommandExecutor
import io.github.starwishsama.nbot.commands.CommandProps
import io.github.starwishsama.nbot.commands.interfaces.UniversalCommand
import io.github.starwishsama.nbot.enums.UserLevel
import io.github.starwishsama.nbot.objects.BotUser
import io.github.starwishsama.nbot.util.limitStringSize
import net.mamoe.mirai.message.MessageEvent
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.asMessageChain
import net.mamoe.mirai.message.data.toMessage

class HelpCommand : UniversalCommand {
    override suspend fun execute(event: MessageEvent, args: List<String>, user: BotUser): MessageChain {
        val sb = StringBuilder()
        for (cmd in CommandExecutor.commands) {
            if (cmd.getProps().name.contentEquals("help") || !cmd.getProps().name.contentEquals("debug")) {
                sb.append("/").append(cmd.getProps().name).append("  ").append(cmd.getProps().description).append("\n")
            }
        }

        return sb.toString().trim().limitStringSize(200).toMessage().asMessageChain()
    }

    override fun getProps(): CommandProps =
        CommandProps("help", arrayListOf("?", "帮助", "菜单"), "帮助命令", "nbot.commands.help", UserLevel.USER)

    // 它自己就是帮助命令 不需要再帮了
    override fun getHelp(): String = ""
}