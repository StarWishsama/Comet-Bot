package io.github.starwishsama.comet.commands.console

import io.github.starwishsama.comet.BotVariables.daemonLogger
import io.github.starwishsama.comet.api.annotations.CometCommand
import io.github.starwishsama.comet.api.command.CommandProps
import io.github.starwishsama.comet.api.command.interfaces.ConsoleCommand
import io.github.starwishsama.comet.enums.UserLevel
import io.github.starwishsama.comet.file.DataSetup
import io.github.starwishsama.comet.objects.BotUser
import io.github.starwishsama.comet.utils.StringUtil.isNumeric
import java.io.IOException

@CometCommand
class AdminCommand : ConsoleCommand {
    override suspend fun execute(args: List<String>): String {
        if (args.isNotEmpty()) {
            when (args[0]) {
                "upgrade" -> {
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
                "setowner" -> {
                    if (args.size > 1 && args[1].isNumeric()) {
                        val target = BotUser.getUserSafely(args[1].toLong())
                        target.level = UserLevel.OWNER
                        return "成功将 ${target.id} 设为 ${target.level.name}"
                    }
                }
                "reload" -> {
                    try {
                        DataSetup.reload()
                        return "重载成功."
                    } catch (e: IOException) {
                        daemonLogger.warning("在重载时发生了异常", e)
                    }
                }
            }
        }
        return ""
    }

    override fun getProps(): CommandProps = CommandProps("admin", mutableListOf(), "", "", UserLevel.CONSOLE)
}