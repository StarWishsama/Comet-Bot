package ren.natsuyuk1.comet.telegram.util

import dev.inmo.tgbotapi.abstracts.FromUser
import dev.inmo.tgbotapi.types.chat.GroupChat
import dev.inmo.tgbotapi.types.message.abstracts.CommonMessage
import dev.inmo.tgbotapi.types.message.content.MessageContent

fun CommonMessage<MessageContent>.format(): String {
    val msg = this@format

    val prefix: String = when (chat) {
        is GroupChat -> "[${(chat as GroupChat).title} (${chat.id.chatId})]"
        else -> ""
    }

    val sender = buildString {
        if (msg is FromUser) {
            append("${msg.from.getDisplayName()}(${msg.from.id})")
        }
    }

    return "${prefix}$sender -> ${msg.content}"
}
