package io.github.starwishsama.comet.commands.chats

import io.github.starwishsama.comet.api.annotations.CometCommand
import io.github.starwishsama.comet.api.command.CommandProps
import io.github.starwishsama.comet.api.command.interfaces.ChatCommand
import io.github.starwishsama.comet.enums.UserLevel
import io.github.starwishsama.comet.objects.BotUser
import io.github.starwishsama.comet.utils.CometUtil
import io.github.starwishsama.comet.utils.CometUtil.sendMessage
import io.github.starwishsama.comet.utils.StringUtil.convertToChain
import io.github.starwishsama.comet.utils.StringUtil.isNumeric
import io.github.starwishsama.comet.utils.network.MinecraftUtil
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.EmptyMessageChain
import net.mamoe.mirai.message.data.MessageChain
import java.io.IOException

@CometCommand
class MinecraftCommand: ChatCommand {
    override suspend fun execute(event: MessageEvent, args: List<String>, user: BotUser): MessageChain {
        if (CometUtil.isNoCoolDown(user.id)) {
            if (args.isEmpty()) return getHelp().convertToChain()

            when (args.size) {
                1 -> {
                    val convert = MinecraftUtil.convert(args[0])
                    return if (!convert.success) {
                        "无法连接至服务器".sendMessage()
                    } else {
                        try {
                            val result =  MinecraftUtil.query(convert.host, convert.port)
                            result.toString().sendMessage()
                        } catch (e: Exception) {
                            "查询失败, 服务器可能不在线, 请稍后再试.".sendMessage()
                        }
                    }
                }
                2 -> {
                    return if (args[1].isNumeric()) {
                        try {
                            val result = MinecraftUtil.query(args[0], args[1].toInt())
                            result.toString().sendMessage()
                        } catch (e: IOException) {
                            "查询失败, 服务器可能不在线, 请稍后再试.".sendMessage()
                        } catch (e: NumberFormatException) {
                            "输入的端口号不合法.".sendMessage()
                        }
                    } else {
                        "输入的端口号不合法.".sendMessage()
                    }
                }
            }
        }

        return EmptyMessageChain
    }

    override fun getProps(): CommandProps = CommandProps(
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
}