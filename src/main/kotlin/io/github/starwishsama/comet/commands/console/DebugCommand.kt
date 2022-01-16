/*
 * Copyright (c) 2019-2022 StarWishsama.
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
import io.github.starwishsama.comet.api.command.CommandManager
import io.github.starwishsama.comet.sessions.SessionHandler
import io.github.starwishsama.comet.utils.CometUtil
import io.github.starwishsama.comet.utils.RuntimeUtil
import net.mamoe.mirai.console.command.CompositeCommand
import net.mamoe.mirai.console.command.ConsoleCommandOwner
import net.mamoe.mirai.console.command.ConsoleCommandSender

object DebugCommand : CompositeCommand(ConsoleCommandOwner, "debug") {
    @SubCommand
    suspend fun ConsoleCommandSender.sessions() {
        val sessions = SessionHandler.getSessions()
        sendMessage(buildString {
            append("目前活跃的会话列表: \n")
            if (sessions.isEmpty()) {
                append("无")
                return@buildString
            }

            for ((i, session) in sessions.withIndex()) {
                append(i + 1).append(" ").append(session.toString()).append("\n")
            }
        }.trim())
    }

    @SubCommand
    suspend fun ConsoleCommandSender.switch() {
        CometVariables.switch = !CometVariables.switch
        sendMessage("Bot > 维护模式已${if (!CometVariables.switch) "开启" else "关闭"}")
    }

    @SubCommand
    suspend fun ConsoleCommandSender.info() {
        sendMessage(
            "彗星 Bot ${BuildConfig.version}\n" +
                    "Comet 状态: ${comet.getBot().isOnline} | ${CometVariables.switch}\n" +
                    "已注册命令数: ${CommandManager.countCommands()}\n" +
                    CometUtil.getMemoryUsage() + "\n" +
                    "CPU 负载: ${RuntimeUtil.getOperatingSystemBean().systemLoadAverage}\n" +
                    "构建时间: ${BuildConfig.buildTime}"
        )
    }
}