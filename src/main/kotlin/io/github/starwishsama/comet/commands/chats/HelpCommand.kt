package io.github.starwishsama.comet.commands.chats

import io.github.starwishsama.comet.api.annotations.CometCommand
import io.github.starwishsama.comet.api.command.CommandExecutor
import io.github.starwishsama.comet.api.command.CommandProps
import io.github.starwishsama.comet.api.command.interfaces.ChatCommand
import io.github.starwishsama.comet.enums.UserLevel
import io.github.starwishsama.comet.objects.BotUser
import io.github.starwishsama.comet.utils.BotUtil
import io.github.starwishsama.comet.utils.StringUtil.convertToChain
import net.mamoe.mirai.message.MessageEvent
import net.mamoe.mirai.message.data.EmptyMessageChain
import net.mamoe.mirai.message.data.MessageChain

@CometCommand
class HelpCommand : ChatCommand {
    override suspend fun execute(event: MessageEvent, args: List<String>, user: BotUser): MessageChain {
        if (BotUtil.hasNoCoolDown(event.sender.id)) {
            if (args.isEmpty()) {
                val sb = buildString {
                    append(BotUtil.sendMessageAsString("可用的命令:\n"))
                    for (cmd in CommandExecutor.getCommands()) {
                        if (!cmd.isHidden) {
                            append("/").append(cmd.getProps().name).append(",")
                        }
                    }
                }.removeSuffix(",").plus("]")

                return sb.trim().convertToChain()
            } else {
                val cmd = CommandExecutor.getCommand(args[0])
                return if (cmd != null) {
                    BotUtil.sendMessage("关于 /${cmd.name} 的帮助信息\n${cmd.getHelp()}")
                } else {
                    BotUtil.sendMessage("该命令不存在哦")
                }
            }
        }
        return EmptyMessageChain
    }

    override fun getProps(): CommandProps =
            CommandProps("help", arrayListOf("?", "帮助", "菜单"), "帮助命令", "nbot.commands.help", UserLevel.USER)

    // 它自己就是帮助命令 不需要再帮了
    override fun getHelp(): String = ""

    override val isHidden: Boolean
        get() = true
}