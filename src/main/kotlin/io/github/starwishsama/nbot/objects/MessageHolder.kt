package io.github.starwishsama.nbot.objects

import net.mamoe.mirai.message.data.EmptyMessageChain
import net.mamoe.mirai.message.data.MessageChain

data class MessageHolder(val rawMessage: String, val messageChain: MessageChain?) {
    override fun toString(): String {
        return messageChain?.contentToString() ?: rawMessage
    }

    fun isEmpty(): Boolean {
        return if (messageChain == null) rawMessage.isEmpty() else messageChain is EmptyMessageChain
    }
}