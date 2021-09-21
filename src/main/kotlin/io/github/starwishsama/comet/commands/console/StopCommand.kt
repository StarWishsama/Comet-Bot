/*
 * Copyright (c) 2019-2021 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package io.github.starwishsama.comet.commands.console


import io.github.starwishsama.comet.api.command.CommandProps
import io.github.starwishsama.comet.api.command.interfaces.ConsoleCommand
import io.github.starwishsama.comet.objects.enums.UserLevel
import kotlin.system.exitProcess


class StopCommand : ConsoleCommand {
    override suspend fun execute(args: List<String>): String {
        exitProcess(0)
    }

    override fun getProps(): CommandProps =
        CommandProps("stop", mutableListOf(), "", "", UserLevel.CONSOLE)

    override fun getHelp(): String = ""
}