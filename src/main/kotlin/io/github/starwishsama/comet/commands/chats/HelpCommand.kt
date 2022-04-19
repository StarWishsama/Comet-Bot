/*
 * Copyright (c) 2019-2021 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package io.github.starwishsama.comet.commands.chats


import io.github.starwishsama.comet.api.command.CommandManager
import io.github.starwishsama.comet.api.command.CommandProps
import io.github.starwishsama.comet.api.command.interfaces.ChatCommand
import io.github.starwishsama.comet.objects.CometUser
import io.github.starwishsama.comet.objects.enums.UserLevel
import io.github.starwishsama.comet.utils.CometUtil
import io.github.starwishsama.comet.utils.CometUtil.toMessageChain
import io.github.starwishsama.comet.utils.StringUtil.convertToChain
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.MessageChain

object HelpCommand : ChatCommand {
    override suspend fun execute(event: MessageEvent, args: List<String>, user: CometUser): MessageChain {
        if (args.isEmpty()) {
            val sb = buildString {
                append(CometUtil.sendMessageAsString("可用的命令:"))
                append("\n[")
                for (cmd in CommandManager.getCommands()) {
                    if (!cmd.isHidden) {
                        append(cmd.props.name).append(", ")
                    }
                }
            }.removeSuffix(", ").plus("]")

            return sb.trim().convertToChain()
        } else {
            val cmd = CommandManager.getCommand(args[0])
            return if (cmd != null) {
                buildString {
                    appendLine("关于 /${cmd.name} 的帮助信息")
                    appendLine(cmd.getHelp())
                    if (cmd.props.aliases.isNotEmpty()) {
                        appendLine()
                        appendLine("该命令还有其他别名可以使用: ${cmd.props.aliases}")
                    }
                }.toMessageChain()
            } else {
                toMessageChain("该命令不存在哦")
            }
        }
    }

    override val props: CommandProps =
        CommandProps("help", arrayListOf("?", "帮助", "菜单"), "帮助命令", UserLevel.USER)

    // 它自己就是帮助命令 不需要再帮了
    override fun getHelp(): String = ""

    override val isHidden: Boolean
        get() = true
}
