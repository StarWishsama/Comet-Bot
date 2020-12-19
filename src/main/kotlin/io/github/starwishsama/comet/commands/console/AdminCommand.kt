package io.github.starwishsama.comet.commands.console

import io.github.starwishsama.comet.BotVariables.daemonLogger
import io.github.starwishsama.comet.BotVariables.users
import io.github.starwishsama.comet.api.annotations.CometCommand
import io.github.starwishsama.comet.api.command.CommandProps
import io.github.starwishsama.comet.api.command.interfaces.ConsoleCommand
import io.github.starwishsama.comet.enums.UserLevel
import io.github.starwishsama.comet.file.DataSetup
import io.github.starwishsama.comet.managers.GroupConfigManager
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

                        val targetLevel = target.level.ordinal + 1

                        if (targetLevel > UserLevel.values().size) {
                            target.level = UserLevel.USER
                        } else {
                            target.level = UserLevel.values()[target + 1]
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
                "resetpoint", "rpoint", "rp" -> {
                    if (args.size > 1 && args[1].isNumeric()) {
                        val time = try {
                            args[1].toInt()
                        } catch (e: NumberFormatException) {
                            return "输入的数字不合法! 范围: (0, 300]"
                        }

                        return if (time in 1..300) {
                            users.parallelStream().forEach { it.commandTime = time }
                            "成功重置所有 BotUser 的积分"
                        } else {
                            "输入的数字错误! 范围: (0, 300]"
                        }
                    }
                }
                "cmd" -> {
                    if (args.size > 2 && args[1].isNumeric()) {
                        val gid = try {
                            args[1].toLong()
                        } catch (e: NumberFormatException) {
                            return "输入的群号不合法!"
                        }

                        return GroupConfigManager.getConfig(gid)?.disableCommand(args[2])?.msg ?:"输入的群号没有配置文件或不存在!"
                    }
                }
            }
        }
        return ""
    }

    override fun getProps(): CommandProps = CommandProps("admin", mutableListOf(), "", "", UserLevel.CONSOLE)
}