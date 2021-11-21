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

object InfoCommand : ConsoleCommand {
    override suspend fun execute(args: List<String>): String {
        TODO("Not yet implemented")
    }

    override fun getProps(): CommandProps {
        TODO("Not yet implemented")
    }

    override fun getHelp(): String {
        TODO("Not yet implemented")
    }
}