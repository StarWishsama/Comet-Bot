package io.github.starwishsama.nbot.commands.interfaces

import io.github.starwishsama.nbot.commands.CommandProps
import io.github.starwishsama.nbot.objects.BotUser
import net.mamoe.mirai.message.ContactMessage
import net.mamoe.mirai.message.data.MessageChain

interface UniversalCommand {
    suspend fun execute(message: ContactMessage, args: List<String>, user: BotUser) : MessageChain
    fun getProps() : CommandProps
}