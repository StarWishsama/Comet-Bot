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
import io.github.starwishsama.comet.api.command.CommandProps
import io.github.starwishsama.comet.api.command.interfaces.ConsoleCommand
import io.github.starwishsama.comet.objects.enums.UserLevel
import io.github.starwishsama.comet.utils.CometUtil.getRestString
import io.github.starwishsama.comet.utils.StringUtil.isNumeric
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

class BroadcastCommand : ConsoleCommand {
    override suspend fun execute(args: List<String>): String {
        return if (args.size > 1 && args[0].isNumeric()) {
            sendMessage(args[0].toLong(), args.getRestString(1))
        } else if (args[0] == "all") {
            sendMessage(-1, args.getRestString(1))
        } else {
            getHelp()
        }
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

    override fun getProps(): CommandProps = CommandProps("broadcast", mutableListOf("bc"), "", UserLevel.CONSOLE)

    override fun getHelp(): String {
        return "/bc [群号] [发送内容]"
    }
}