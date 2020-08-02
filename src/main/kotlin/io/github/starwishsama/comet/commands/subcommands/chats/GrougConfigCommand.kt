package io.github.starwishsama.comet.commands.subcommands.chats

import io.github.starwishsama.comet.commands.CommandProps
import io.github.starwishsama.comet.commands.interfaces.ChatCommand
import io.github.starwishsama.comet.objects.BotUser
import net.mamoe.mirai.message.MessageEvent
import net.mamoe.mirai.message.data.MessageChain

class GrougConfigCommand : ChatCommand {
    override suspend fun execute(event: MessageEvent, args: List<String>, user: BotUser): MessageChain {
        TODO("Not yet implemented")
    }

    override fun getProps(): CommandProps {
        TODO("Not yet implemented")
    }

    override fun getHelp(): String = """
        
    """.trimIndent()
}