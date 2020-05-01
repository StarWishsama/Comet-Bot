package io.github.starwishsama.nbot.commands.subcommands

import io.github.starwishsama.nbot.commands.CommandProps
import io.github.starwishsama.nbot.commands.interfaces.UniversalCommand
import io.github.starwishsama.nbot.objects.BotUser
import net.mamoe.mirai.message.ContactMessage
import net.mamoe.mirai.message.data.MessageChain

class ShopCommand : UniversalCommand {
    override suspend fun execute(message: ContactMessage, args: List<String>, user: BotUser): MessageChain {
        TODO("Not yet implemented")
    }

    override fun getProps(): CommandProps {
        TODO("Not yet implemented")
    }

    override fun getHelp(): String {
        TODO("Not yet implemented")
    }
}