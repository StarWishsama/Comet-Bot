/*
 * Copyright (c) 2019-2022 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 * Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 */

package ren.natsuyuk1.comet.commands

import moe.sdl.yac.parameters.arguments.argument
import moe.sdl.yac.parameters.arguments.default
import moe.sdl.yac.parameters.options.convert
import moe.sdl.yac.parameters.options.default
import moe.sdl.yac.parameters.options.option
import moe.sdl.yac.parameters.types.int
import ren.natsuyuk1.comet.api.command.CometCommand
import ren.natsuyuk1.comet.api.command.CommandManager
import ren.natsuyuk1.comet.api.command.CommandProperty
import ren.natsuyuk1.comet.api.message.MessageWrapper
import ren.natsuyuk1.comet.user.CometUser

private val property by lazy {
    CommandProperty(
        "help",
        listOf("?"),
        "展示 Comet 的帮助菜单",
        "输入 /help 查询命令列表"
    )
}

abstract class HelpCommand(raw: String, message: MessageWrapper, user: CometUser) :
    CometCommand(raw, message, user, property) {

    private val pageNum by argument(
        name = "帮助菜单的页数"
    ).int().default(1)

    private val pageSize by option(
        "--page-size", "-s",
        help = "帮助菜单一页展示的命令个数",
    ).int().convert {
        it.coerceIn(1..20)
    }.default(10)

    override suspend fun run() {
        val cmds = CommandManager.getCommands()


    }
}
