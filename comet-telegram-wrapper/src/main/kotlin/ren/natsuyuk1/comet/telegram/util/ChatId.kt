package ren.natsuyuk1.comet.telegram.util

import com.github.kotlintelegrambot.entities.ChatId

fun Long.chatID() = ChatId.fromId(this)
