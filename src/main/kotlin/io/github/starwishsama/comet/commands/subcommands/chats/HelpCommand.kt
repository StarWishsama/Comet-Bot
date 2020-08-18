package io.github.starwishsama.comet.commands.subcommands.chats

import io.github.starwishsama.comet.commands.CommandExecutor
import io.github.starwishsama.comet.commands.CommandProps
import io.github.starwishsama.comet.commands.interfaces.ChatCommand
import io.github.starwishsama.comet.enums.UserLevel
import io.github.starwishsama.comet.objects.BotUser
import io.github.starwishsama.comet.utils.BotUtil
import io.github.starwishsama.comet.utils.StringUtil.convertToChain
import io.github.starwishsama.comet.utils.StringUtil.limitStringSize
import net.mamoe.mirai.message.MessageEvent
import net.mamoe.mirai.message.data.EmptyMessageChain
import net.mamoe.mirai.message.data.MessageChain

class HelpCommand : ChatCommand {
    override suspend fun execute(event: MessageEvent, args: List<String>, user: BotUser): MessageChain {
        if (BotUtil.hasNoCoolDown(event.sender.id)) {
            val sb = StringBuilder()
            for (cmd in CommandExecutor.getCommands()) {
                if (cmd.getProps().name.contentEquals("help") || !cmd.getProps().name.contentEquals("debug")) {
                    sb.append("/").append(cmd.getProps().name).append("  ").append(cmd.getProps().description).append("\n")
                }
            }

            return sb.toString().trim().limitStringSize(200).convertToChain()
        }
        return EmptyMessageChain
    }

    override fun getProps(): CommandProps =
        CommandProps("help", arrayListOf("?", "帮助", "菜单"), "帮助命令", "nbot.commands.help", UserLevel.USER)

    // 它自己就是帮助命令 不需要再帮了
    override fun getHelp(): String = ""
}