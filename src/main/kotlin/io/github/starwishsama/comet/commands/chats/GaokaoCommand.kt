package io.github.starwishsama.comet.commands.chats

import io.github.starwishsama.comet.api.command.CommandProps
import io.github.starwishsama.comet.api.command.interfaces.ChatCommand
import io.github.starwishsama.comet.objects.CometUser
import io.github.starwishsama.comet.objects.enums.UserLevel
import io.github.starwishsama.comet.utils.StringUtil.getLastingTimeAsString
import io.github.starwishsama.comet.utils.gaokaoDateTime
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.QuoteReply
import net.mamoe.mirai.message.data.buildMessageChain
import java.util.concurrent.TimeUnit

object GaokaoCommand : ChatCommand {
    override suspend fun execute(event: MessageEvent, args: List<String>, user: CometUser): MessageChain =
        buildMessageChain {
            add(QuoteReply(event.source))
            add("现在距离2022年普通高等学校招生全国统一考试还有${gaokaoDateTime.getLastingTimeAsString(TimeUnit.DAYS)}。")
        }

    override val props: CommandProps = CommandProps(
        name = "gaokao",
        aliases = listOf("gk"),
        description = "查询高考时间",
        level = UserLevel.USER,
    )

    override fun getHelp(): String = "/gk 查询高考时间"
}
