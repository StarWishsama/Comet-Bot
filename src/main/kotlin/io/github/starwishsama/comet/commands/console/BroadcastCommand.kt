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

import io.github.starwishsama.comet.CometVariables.comet
import io.github.starwishsama.comet.CometVariables.daemonLogger
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.console.command.ConsoleCommandOwner
import net.mamoe.mirai.console.command.ConsoleCommandSender
import net.mamoe.mirai.console.command.SimpleCommand

object BroadcastCommand : SimpleCommand(ConsoleCommandOwner, "broadcast", description = "发送广播") {
    @Handler
    suspend fun ConsoleCommandSender.handle(message: String) {
        sendMessage(sendMessage(-1, message))
    }

    @Handler
    suspend fun ConsoleCommandSender.handle(groupId: Long, message: String) {
        sendMessage(sendMessage(groupId, message))
    }

    private fun sendMessage(groupId: Long, message: String): String {
        if (groupId == -1L) {
            comet.getBot().groups.forEach {
                runBlocking {
                    try {
                        it.sendMessage(message)
                        delay(1_500)
                    } catch (e: RuntimeException) {
                        daemonLogger.warning("发送失败", e)
                    }
                }
            }

            return ""
        }

        val g = comet.getBot().getGroup(groupId) ?: return "找不到群号对应的群"

        return runBlocking {
            try {
                g.sendMessage(message)
                return@runBlocking "发送成功!"
            } catch (e: RuntimeException) {
                daemonLogger.warning(e.stackTraceToString())
                return@runBlocking "发送失败, 错误信息: ${e.message}"
            }
        }
    }
}