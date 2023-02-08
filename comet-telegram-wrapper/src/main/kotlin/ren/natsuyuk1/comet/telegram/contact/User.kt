package ren.natsuyuk1.comet.telegram.contact

import ren.natsuyuk1.comet.api.user.User
import ren.natsuyuk1.comet.telegram.TelegramComet
import ren.natsuyuk1.comet.telegram.util.getDisplayName

class TelegramUser(
    override val contact: dev.inmo.tgbotapi.types.chat.User,
    override val comet: TelegramComet
) : User, TelegramContact {
    override val name: String = contact.getDisplayName()
    override val id: Long = contact.id.chatId
}

fun dev.inmo.tgbotapi.types.chat.User.toCometUser(comet: TelegramComet) = TelegramUser(this, comet)
