package io.github.starwishsama.nbot.commands.interfaces

import io.github.starwishsama.nbot.commands.CommandProps
import net.mamoe.mirai.message.FriendMessage
import net.mamoe.mirai.message.data.MessageChain

interface FriendCommand {
    suspend fun executeFriend(message: FriendMessage) : MessageChain
    fun getProps() : CommandProps
}