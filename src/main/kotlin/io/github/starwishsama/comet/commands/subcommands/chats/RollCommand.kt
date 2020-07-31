package io.github.starwishsama.comet.commands.subcommands.chats

import io.github.starwishsama.comet.commands.CommandProps
import io.github.starwishsama.comet.commands.interfaces.ChatCommand
import io.github.starwishsama.comet.enums.UserLevel
import io.github.starwishsama.comet.objects.BotUser
import net.mamoe.mirai.message.MessageEvent
import net.mamoe.mirai.message.data.MessageChain

class RollCommand : ChatCommand {
    override suspend fun execute(event: MessageEvent, args: List<String>, user: BotUser): MessageChain {
        TODO("Not yet implemented")
    }

    override fun getProps(): CommandProps = CommandProps(
        "roll",
        arrayListOf("rl", "抽奖"),
        "roll东西",
        "nbot.commands.roll",
        UserLevel.USER
    )

    override fun getHelp(): String = """
        /roll [需roll的东西] [roll的个数]
    """.trimIndent()
}