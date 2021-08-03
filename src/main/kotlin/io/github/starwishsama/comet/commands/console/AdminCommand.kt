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

import io.github.starwishsama.comet.CometVariables
import io.github.starwishsama.comet.CometVariables.daemonLogger
import io.github.starwishsama.comet.api.command.CommandManager
import io.github.starwishsama.comet.api.command.CommandProps
import io.github.starwishsama.comet.api.command.interfaces.ConsoleCommand
import io.github.starwishsama.comet.enums.UserLevel
import io.github.starwishsama.comet.file.DataSetup
import io.github.starwishsama.comet.objects.CometUser
import io.github.starwishsama.comet.utils.StringUtil.isNumeric
import java.io.IOException

class AdminCommand : ConsoleCommand {
    override suspend fun execute(args: List<String>): String {
        if (args.isNotEmpty()) {
            when (args[0]) {
                "upgrade" -> {
                    when (args.size) {
                        2 -> {
                            if (args[1].isNumeric()) {
                                val target = CometUser.getUser(args[1].toLong()) ?: return "目标没有使用过 Comet"

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
                                    val target = CometUser.getUser(args[1].toLong()) ?: return "目标没有使用过 Comet"
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
                "cmd" -> {
                    if (args.size > 2 && args[1].isNumeric()) {
                        val gid = try {
                            args[1].toLong()
                        } catch (e: NumberFormatException) {
                            return "输入的群号不合法!"
                        }

                        val cmd = CommandManager.getCommand(args[2])

                        return cmd?.props?.disableCommand(gid)?.msg ?: "找不到对应命令"
                    }
                }
                "groups" -> {
                    return buildString {
                        append("已加入的群聊:\n")
                        CometVariables.comet.getBot().groups.forEach {
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