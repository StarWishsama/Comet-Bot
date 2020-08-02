package io.github.starwishsama.comet.commands.subcommands.console

import io.github.starwishsama.comet.commands.CommandProps
import io.github.starwishsama.comet.commands.interfaces.ConsoleCommand
import io.github.starwishsama.comet.enums.UserLevel
import io.github.starwishsama.comet.objects.BotUser
import io.github.starwishsama.comet.utils.isNumeric

class AdminCommand : ConsoleCommand {
    override suspend fun execute(args: List<String>): String {
        if (args.isNotEmpty()) {
            when (args[0]) {
                "admin" -> {
                    if (args.size > 1 && args[1].isNumeric()) {
                        var target = BotUser.getUser(args[1].toLong())
                        if (target == null) {
                            target = BotUser.quickRegister(args[1].toLong())
                        }

                        if (target.level < UserLevel.ADMIN) {
                            target.level = UserLevel.USER
                        } else {
                            target.level = UserLevel.ADMIN
                        }
                        return "成功将 ${target.id} 设为 ${target.level.name}"
                    }
                }
            }
        }
        return ""
    }

    override fun getProps(): CommandProps = CommandProps("admin", null, "", "", UserLevel.CONSOLE)
}