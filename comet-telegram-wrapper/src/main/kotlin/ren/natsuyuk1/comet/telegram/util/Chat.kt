package ren.natsuyuk1.comet.telegram.util

import dev.inmo.tgbotapi.types.chat.User
import ren.natsuyuk1.comet.utils.string.blankIfNull

fun User.getDisplayName() =
    username?.usernameWithoutAt ?: (this.firstName.blankIfNull() + " " + this.lastName.blankIfNull()).trim()
