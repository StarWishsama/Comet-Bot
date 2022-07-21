package ren.natsuyuk1.comet.telegram.util

import com.github.kotlintelegrambot.entities.Chat

fun Chat.getDisplayName() = this.firstName + " " + this.lastName

fun Chat.usernameOrDisplay() = this.username ?: getDisplayName()
