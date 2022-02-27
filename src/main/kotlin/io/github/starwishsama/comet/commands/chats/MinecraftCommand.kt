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
import io.github.starwishsama.comet.objects.CometUser
import io.github.starwishsama.comet.objects.enums.UserLevel
import io.github.starwishsama.comet.utils.CometUtil.toMessageChain
import io.github.starwishsama.comet.utils.StringUtil.convertToChain
import io.github.starwishsama.comet.utils.StringUtil.isNumeric
import io.github.starwishsama.comet.utils.network.MinecraftUtil
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.time.withTimeout
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.MessageChain
import java.time.Duration

object MinecraftCommand : ChatCommand {
    override suspend fun execute(event: MessageEvent, args: List<String>, user: CometUser): MessageChain {

        if (args.isEmpty()) return getHelp().convertToChain()

        when (args.size) {
            1 -> {
                if (args[0].contains(":")) {
                    val split = args[0].split(":")
                    event.subject.sendMessage(toMessageChain("查询中..."))

                    return query(split[0], split[1].toIntOrNull(), event.subject)
                }

                val convert = MinecraftUtil.convert(args[0])
                return if (convert.isEmpty()) {
                    "无法连接至服务器".toMessageChain()
                } else {
                    event.subject.sendMessage(toMessageChain("查询中..."))
                    query(convert.host, convert.port, event.subject)
                }
            }
            2 -> {
                return if (args[1].isNumeric()) {
                    event.subject.sendMessage(toMessageChain("查询中..."))
                    query(args[0], args[1].toIntOrNull(), event.subject)
                } else {
                    "输入的端口号不合法.".toMessageChain()
                }
            }
            else -> return getHelp().toMessageChain()
        }
    }

    override val props: CommandProps = CommandProps(
        "mc",
        listOf("mcquery", "mq", "mccx", "minecraft"),
        "查询我的世界服务器信息",
        UserLevel.USER,
        cost = 5.0
    )

    override fun getHelp(): String = """
        /mc [服务器地址] [服务器端口] 查询服务器信息
        /mc [服务器地址] 查询服务器信息 (使用 SRV)
    """.trimIndent()

    private suspend fun query(ip: String, port: Int?, subject: Contact): MessageChain {
        return runCatching<MessageChain> {
            return withTimeout(Duration.ofSeconds(5)) {
                return@withTimeout runCatching<MessageChain> handleQuery@ {
                    if (port == null) {
                        return@handleQuery "输入的端口号不合法.".toMessageChain()
                    }

                    val javaResult = MinecraftUtil.javaQuery(ip, port)
                    val javaWrapper = javaResult.convertToWrapper()

                    return@handleQuery if (!javaWrapper.isEmpty()) {
                        javaWrapper.toMessageChain(subject)
                    } else {
                        val bedrockResult = MinecraftUtil.bedrockQuery(ip, port)
                        val bedrockWrapper = bedrockResult.convertToWrapper()
                        if (!bedrockWrapper.isEmpty()) {
                            bedrockResult.convertToWrapper().toMessageChain(subject)
                        } else {
                            "查询失败, 服务器可能不在线, 请稍后再试.".toMessageChain()
                        }
                    }
                }.getOrElse { "查询失败, 提供的地址不正确或服务器不在线, 请稍后再试.".toMessageChain() }
            }
        }.onFailure {
            if (it is TimeoutCancellationException) {
                return "连接至服务器超时, 请稍后再试".toMessageChain()
            }
        }.getOrElse { "查询失败, 提供的地址不正确或服务器不在线, 请稍后再试.".toMessageChain() }
    }
}