package io.github.starwishsama.comet.commands.console

import io.github.starwishsama.comet.BotVariables
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
                    when (args.size) {
                        2 -> {
                            if (args[1].isNumeric()) {
                                val target = BotUser.getUser(args[1].toLong()) ?: return "目标没有使用过 Comet"

                                val targetLevel = target.level.ordinal + 1

                                if (targetLevel >= UserLevel.values().size) {
                                    target.level = UserLevel.USER
                                } else {
                                    target.level = UserLevel.values()[targetLevel]
                                }

                                return "成功将 ${target.id} 设为 ${target.level.name}"
                            }
                        }
                        3 -> {
                            if (args[1].isNumeric()) {
                                try {
                                    val target = BotUser.getUser(args[1].toLong()) ?: return "目标没有使用过 Comet"
                                    val level = UserLevel.valueOf(args[2])
                                    target.level = level

                                    return "成功将 ${target.id} 设为 ${target.level.name}"
                                } catch (e: IllegalArgumentException) {
                                    return "不是有效的等级名字, 可用等级名: ${UserLevel.values()}"
                                }
                            }
                        }
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
                            users.forEach { it.value.commandTime = time }
                            "成功重置所有用户的积分为 $time"
                        } else {
                            "输入的数字错误! 范围: (0, 300]"
                        }
                    }
                }
                "give" -> {
                    if (args.size > 1 && args[1].isNumeric()) {
                        val time = try {
                            args[1].toInt()
                        } catch (e: NumberFormatException) {
                            return "输入的数字不合法! 范围: (0, 10000]"
                        }

                        return if (time in 1..10000) {
                            users.forEach { it.value.addTime(time, true) }
                            "成功给予所有 BotUser 积分 $time 点"
                        } else {
                            "输入的数字错误! 范围: (0, 10000]"
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
                "groups" -> {
                    return buildString {
                        append("已加入的群聊:\n")
                        BotVariables.comet.getBot().groups.forEach {
                            append("${it.name} (${it.id}),")
                        }
                    }.removeSuffix(",").trim()
                }
                else -> return getHelp()
            }
        } else {
            return getHelp()
        }
        return ""
    }

    override fun getProps(): CommandProps = CommandProps("admin", mutableListOf(), "", "", UserLevel.CONSOLE)

    override fun getHelp(): String = """
        /admin upgrade [ID] (权限组名) 修改权限组
        /admin reload 重载配置文件
        /admin rp [积分] 重置所有账号的积分为指定积分数
        /admin cmd [群号] 在指定群禁用命令
    """.trimIndent()
}