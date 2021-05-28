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


import io.github.starwishsama.comet.api.command.CommandExecutor
import io.github.starwishsama.comet.api.command.CommandProps
import io.github.starwishsama.comet.api.command.interfaces.ChatCommand
import io.github.starwishsama.comet.enums.UserLevel
import io.github.starwishsama.comet.objects.BotUser
import io.github.starwishsama.comet.utils.CometUtil
import io.github.starwishsama.comet.utils.StringUtil.convertToChain
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.MessageChain


class HelpCommand : ChatCommand {
    override suspend fun execute(event: MessageEvent, args: List<String>, user: BotUser): MessageChain {
        if (args.isEmpty()) {
            val sb = buildString {
                append(CometUtil.sendMessageAsString("可用的命令:"))
                append("\n[")
                for (cmd in CommandExecutor.getCommands()) {
                    if (!cmd.isHidden) {
                        append(cmd.getProps().name).append(", ")
                    }
                }
            }.removeSuffix(", ").plus("]")

            return sb.trim().convertToChain()
        } else {
            val cmd = CommandExecutor.getCommand(args[0])
            return if (cmd != null) {
                CometUtil.toChain("关于 /${cmd.name} 的帮助信息\n${cmd.getHelp()}")
            } else {
                CometUtil.toChain("该命令不存在哦")
            }
        }
    }

    override fun getProps(): CommandProps =
        CommandProps("help", arrayListOf("?", "帮助", "菜单"), "帮助命令", "nbot.commands.help", UserLevel.USER)

    // 它自己就是帮助命令 不需要再帮了
    override fun getHelp(): String = ""

    override val isHidden: Boolean
        get() = true
}