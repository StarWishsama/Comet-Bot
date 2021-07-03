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

import io.github.starwishsama.comet.BuildConfig
import io.github.starwishsama.comet.CometVariables
import io.github.starwishsama.comet.CometVariables.comet
import io.github.starwishsama.comet.api.command.CommandExecutor
import io.github.starwishsama.comet.api.command.CommandProps
import io.github.starwishsama.comet.api.command.interfaces.ConsoleCommand
import io.github.starwishsama.comet.enums.UserLevel
import io.github.starwishsama.comet.sessions.SessionHandler
import io.github.starwishsama.comet.utils.CometUtil
import io.github.starwishsama.comet.utils.RuntimeUtil
import kotlin.time.ExperimentalTime


class DebugCommand : ConsoleCommand {
    @ExperimentalTime
    override suspend fun execute(args: List<String>): String {
        if (args.isNotEmpty()) {
            when (args[0]) {
                "sessions" -> {
                    val sessions = SessionHandler.getSessions()
                    return buildString {
                        append("目前活跃的会话列表: \n")
                        if (sessions.isEmpty()) {
                            append("无")
                            return@buildString
                        }

                        for ((i, session) in sessions.withIndex()) {
                            append(i + 1).append(" ").append(session.toString()).append("\n")
                        }
                    }.trim()
                }
                "info" ->
                    return ("彗星 Bot ${BuildConfig.version}\n" +
                            "Comet 状态: ${comet.getBot().isOnline} | ${CometVariables.switch}\n" +
                            "已注册命令数: ${CommandExecutor.countCommands()}\n" +
                            CometUtil.getMemoryUsage() + "\n" +
                            "CPU 负载: ${RuntimeUtil.getOperatingSystemBean().systemLoadAverage}\n" +
                            "构建时间: ${BuildConfig.buildTime}"
                            )
                "switch" -> {
                    CometVariables.switch = !CometVariables.switch
                    return "Bot > 维护模式已${if (!CometVariables.switch) "开启" else "关闭"}"
                }
                else -> return getHelp()
            }
        }
        return ""
    }

    override fun getProps(): CommandProps = CommandProps("debug", mutableListOf(), "", "", UserLevel.CONSOLE)

    override fun getHelp(): String = "请自行查阅代码"
}