package ren.natsuyuk1.comet.telegram.util

import dev.inmo.tgbotapi.types.chat.ChatPermissions

val MUTE = ChatPermissions(
    canSendMessages = false,
    canSendAudios = false,
    canSendDocuments = false,
    canSendPhotos = false,
    canSendVideoNotes = false,
    canSendVoiceNotes = false,
    canSendPolls = false,
    canSendOtherMessages = false,
)

val UNMUTE = ChatPermissions(
    canSendMessages = true,
    canSendAudios = true,
    canSendDocuments = true,
    canSendPhotos = true,
    canSendVideoNotes = true,
    canSendVoiceNotes = true,
    canSendPolls = true,
    canSendOtherMessages = true,
)
