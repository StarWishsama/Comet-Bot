package io.github.starwishsama.comet.commands.subcommands.chats

import io.github.starwishsama.comet.annotations.CometCommand
import io.github.starwishsama.comet.commands.CommandProps
import io.github.starwishsama.comet.commands.interfaces.ChatCommand
import io.github.starwishsama.comet.enums.UserLevel
import io.github.starwishsama.comet.objects.BotUser
import io.github.starwishsama.comet.utils.BotUtil
import io.github.starwishsama.comet.utils.StringUtil.convertToChain
import net.mamoe.mirai.message.MessageEvent
import net.mamoe.mirai.message.data.EmptyMessageChain
import net.mamoe.mirai.message.data.MessageChain

@CometCommand
class YoutubeCommand : ChatCommand {
    override suspend fun execute(event: MessageEvent, args: List<String>, user: BotUser): MessageChain {
        if (BotUtil.hasNoCoolDown(user.id)) {
            if (args.isEmpty()) {
                return getHelp().convertToChain()
            } else {
                when (args[0]) {
                    "info" -> TODO("Youtube command haven't been implemented")
                    "sub" -> TODO("Youtube command haven't been implemented")
                    "unsub" -> TODO("Youtube command haven't been implemented")
                    "push" -> TODO("Youtube command haven't been implemented")
                    else -> getHelp().convertToChain()
                }
            }
        }
        return EmptyMessageChain
    }

    override fun getProps(): CommandProps =
        CommandProps(
            name = "youtube",
            aliases = listOf("ytb", "y2b", "油管", "油土鳖"),
            description = "查询 Youtube 频道信息",
            permission = "nbot.commands.youtube",
            level = UserLevel.ADMIN
        )

    override fun getHelp(): String = """
        /youtube info [频道ID] 查询某个频道的信息
        /youtube sub [频道ID] 订阅某个频道的开播消息
        /youtube unsub [频道ID] 退订某个频道的开播消息
        /youtube push 开关本群推送功能
        
        该命令亦可用 /ytb|y2b|油管 等使用
        频道 ID 即为频道链接 channel 后面的一串字
    """.trimIndent()
}