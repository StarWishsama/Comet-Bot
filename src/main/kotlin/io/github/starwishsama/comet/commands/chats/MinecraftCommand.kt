/*
 * Copyright (c) 2019-2021 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package io.github.starwishsama.comet.commands.chats

import io.github.starwishsama.comet.api.command.CommandProps
import io.github.starwishsama.comet.api.command.interfaces.ChatCommand
import io.github.starwishsama.comet.enums.UserLevel
import io.github.starwishsama.comet.objects.CometUser
import io.github.starwishsama.comet.utils.CometUtil.toChain
import io.github.starwishsama.comet.utils.StringUtil.convertToChain
import io.github.starwishsama.comet.utils.StringUtil.isNumeric
import io.github.starwishsama.comet.utils.network.MinecraftUtil
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.MessageChain
import java.io.IOException

class MinecraftCommand : ChatCommand {
    override suspend fun execute(event: MessageEvent, args: List<String>, user: CometUser): MessageChain {

        if (args.isEmpty()) return getHelp().convertToChain()

        when (args.size) {
            1 -> {
                if (args[0].contains(":")) {
                    val split = args[0].split(":")
                    event.subject.sendMessage(toChain("查询中..."))

                    return query(split[0], split[1].toIntOrNull(), event.subject)
                }

                val convert = MinecraftUtil.convert(args[0])
                return if (convert.isEmpty()) {
                    "无法连接至服务器".toChain()
                } else {
                    event.subject.sendMessage(toChain("查询中..."))
                    query(convert.host, convert.port, event.subject)
                }
            }
            2 -> {
                return if (args[1].isNumeric()) {
                    event.subject.sendMessage(toChain("查询中..."))

                    query(args[0], args[1].toIntOrNull(), event.subject)
                } else {
                    "输入的端口号不合法.".toChain()
                }
            }
            else -> return getHelp().toChain()
        }
    }

    override val props: CommandProps = CommandProps(
        "mc",
        listOf("我的世界", "mcquery", "mq", "服务器", "服务器查询", "mccx"),
        "查询我的世界服务器信息",
        "nbot.commands.mc",
        UserLevel.USER
    )

    override fun getHelp(): String = """
        /mc [服务器地址] [服务器端口] 查询服务器信息
        /mc [服务器地址] 查询服务器信息 (使用 SRV)
    """.trimIndent()

    private fun query(ip: String, port: Int?, subject: Contact): MessageChain {
        return try {
            if (port == null) {
                return "输入的端口号不合法.".toChain()
            }
            val result = MinecraftUtil.query(ip, port)
            result.convertToWrapper().toMessageChain(subject)
        } catch (e: IOException) {
            "查询失败, 服务器可能不在线, 请稍后再试.".toChain()
        }
    }
}