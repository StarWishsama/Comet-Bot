package io.github.starwishsama.comet.commands.chats

import io.github.starwishsama.comet.BotVariables
import io.github.starwishsama.comet.api.command.CommandProps
import io.github.starwishsama.comet.api.command.interfaces.ChatCommand
import io.github.starwishsama.comet.enums.UserLevel
import io.github.starwishsama.comet.objects.BotUser
import io.github.starwishsama.comet.service.pusher.PusherManager
import io.github.starwishsama.comet.utils.CometUtil.sendMessage
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.MessageChain

class PusherCommand: ChatCommand {
    override suspend fun execute(event: MessageEvent, args: List<String>, user: BotUser): MessageChain {
        if (args.isEmpty()) return getHelp().sendMessage()

        return when (args[0]) {
            "status", "状态" -> displayPusherStatus()
            "test", "测试" -> testPusher(args)
            else -> {
                """
                    /pusher status 展示所有推送器运行状态
                    /pusher test [推送器名称] 测试一个推送器    
                    """.trimIndent().sendMessage()
            }
        }
    }

    override fun getProps(): CommandProps = CommandProps(
        "pusher",
        arrayListOf("推送器", "tsq"),
        "管理 Comet 的所有推送器",
        "nbot.commands.pusher",
        UserLevel.OWNER
    )

    override fun getHelp(): String =
        "/pusher status 查看所有监听器运行状态\n" +
        "/pusher test [监听器名称] 测试一个监听器是否可用\n" +
        ""

    private fun displayPusherStatus(): MessageChain {
        val ps = PusherManager.getPushers()
        return buildString {
            ps.forEach {
                append(it::class.java.simpleName + "\n")
                append("上次推送了 ${it.pushTime} 次\n")
                append("上次推送于 ${BotVariables.yyMMddPattern.format(it.latestPushTime)}\n")
            }
            trim()
        }.sendMessage()
    }

    private fun testPusher(args: List<String>): MessageChain {
        if (args.size < 2) return "请填写推送器名!".sendMessage()

        val pusher = PusherManager.getPusherByName(args[1]) ?: return "找不到你要测试的推送器".sendMessage()
        pusher.execute()
        return sendMessage("${pusher.name} 运行完成!")
    }
}