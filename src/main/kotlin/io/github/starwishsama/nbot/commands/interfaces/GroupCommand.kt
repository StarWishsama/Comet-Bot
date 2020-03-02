package io.github.starwishsama.nbot.commands.interfaces

import io.github.starwishsama.nbot.commands.CommandProps
import net.mamoe.mirai.message.GroupMessage
import net.mamoe.mirai.message.data.MessageChain

interface GroupCommand {
    suspend fun executeGroup(message: GroupMessage) : MessageChain
    fun getProps() : CommandProps
}