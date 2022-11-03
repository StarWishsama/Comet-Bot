package ren.natsuyuk1.comet.telegram.util

import dev.inmo.tgbotapi.types.chat.ChatPermissions

val MUTE = ChatPermissions(canSendMessages = false, canSendMediaMessages = false, canSendOtherMessages = false)

val UNMUTE = ChatPermissions(canSendMessages = true, canSendMediaMessages = true, canSendOtherMessages = true)
