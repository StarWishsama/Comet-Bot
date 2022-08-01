package ren.natsuyuk1.comet.telegram.util

import com.github.kotlintelegrambot.entities.Chat
import ren.natsuyuk1.comet.utils.string.blankIfNull

fun Chat.getDisplayName() = username ?: (this.firstName.blankIfNull() + " " + this.lastName.blankIfNull()).trim()

fun Chat.usernameOrDisplay() = this.username ?: getDisplayName()
