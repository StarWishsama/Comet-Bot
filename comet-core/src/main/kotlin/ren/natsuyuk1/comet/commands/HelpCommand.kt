/*
 * Copyright (c) 2019-2022 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 * Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 */

package ren.natsuyuk1.comet.commands

import moe.sdl.yac.core.UsageError
import moe.sdl.yac.parameters.arguments.argument
import moe.sdl.yac.parameters.arguments.default
import moe.sdl.yac.parameters.options.convert
import moe.sdl.yac.parameters.options.default
import moe.sdl.yac.parameters.options.option
import moe.sdl.yac.parameters.types.int
import ren.natsuyuk1.comet.api.Comet
import ren.natsuyuk1.comet.api.command.*
import ren.natsuyuk1.comet.api.message.MessageWrapper
import ren.natsuyuk1.comet.api.message.buildMessageWrapper
import ren.natsuyuk1.comet.api.user.CometUser

val HELP by lazy {
    CommandProperty(
        "help",
        listOf("?"),
        "展示 Comet 的帮助菜单",
        "输入 /help 查询命令列表"
    )
}

class HelpCommand(
    comet: Comet,
    sender: PlatformCommandSender,
    subject: PlatformCommandSender,
    message: MessageWrapper,
    user: CometUser
) : CometCommand(comet, sender, subject, message, user, HELP) {

    private val cmdName by argument("命令名称").default("")

    private val pageNum by option(
        "--page-num",
        "-n",
        help = "帮助菜单的页数"
    ).int().default(1)

    private val pageSize by option(
        "--page-size",
        "-s",
        help = "帮助菜单一页展示的命令个数"
    ).int().convert {
        it.coerceIn(1..20)
    }.default(10)

    override suspend fun run() {
        val cmds = CommandManager.getCommands()

        if (cmdName.isNotBlank()) {
            val cmd = CommandManager.getCommand(cmdName, sender)

            if (cmd == null) {
                throw UsageError("找不到名称为 $cmdName 的命令")
            } else {
                subject.sendMessage(
                    buildMessageWrapper {
                        appendTextln("命令 $cmdName")
                        appendLine()
                        appendTextln(cmd.property.description)
                        appendLine()
                        appendText(cmd.property.helpText)
                    }
                )
            }
        } else {
            // Take out the page items from command list.
            val pageItems = cmds.entries.sortedBy { it.key }.chunked(pageSize)
            // Throw exception when the page number exceed.
            if (pageNum !in 1..pageItems.size) {
                throw UsageError("页数超过上限 ${pageItems.size}")
            }

            // Build the message and send to the sender
            subject.sendMessage(
                buildMessageWrapper {
                    appendTextln("Comet 帮助菜单 $pageNum / ${pageItems.size}")
                    val entries = pageItems[pageNum - 1]
                    val maxLen = entries.map { it.key }.maxOf { it.length }
                    entries.filter { it.value !is ConsoleCommandNode }.forEach {
                        val prop = it.value.property

                        appendText(prop.name.padEnd(maxLen, ' '))
                        appendText(" >> ")
                        if (prop.description.isNotBlank()) {
                            appendText(prop.description)
                        } else {
                            appendText("无简介")
                        }
                        appendLine()
                    }
                }
            )
        }
    }
}
