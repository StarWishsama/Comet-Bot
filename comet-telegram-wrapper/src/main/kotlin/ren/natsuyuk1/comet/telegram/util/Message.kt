package ren.natsuyuk1.comet.telegram.util

import com.github.kotlintelegrambot.entities.Message

fun Message.format(): String {
    val msg = this@format

    val prefix = buildString {
        if (msg.chat.title != null) {
            append("[")
            append(msg.chat.title)
            append("(${msg.chat.id})]")
            append(" ")
        }
    }

    val sender = buildString {
        if (msg.senderChat == null) {
            append("${msg.chat.usernameOrDisplay()}(${msg.chat.id})")
        } else {
            append("${msg.senderChat!!.usernameOrDisplay()}(${msg.senderChat!!.id})")
        }
    }

    return "${prefix}$sender -> ${msg.text}"
}
