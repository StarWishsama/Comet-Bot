package ren.natsuyuk1.comet.telegram.util

import dev.inmo.tgbotapi.types.chat.PrivateChat
import dev.inmo.tgbotapi.types.chat.UsernameChat
import ren.natsuyuk1.comet.utils.string.blankIfNull

fun PrivateChat.getDisplayName() =
    username?.usernameWithoutAt ?: (this.firstName.blankIfNull() + " " + this.lastName.blankIfNull()).trim()

fun UsernameChat.getDisplayName() =
    username?.usernameWithoutAt ?: ""
