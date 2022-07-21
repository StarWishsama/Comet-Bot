package ren.natsuyuk1.comet.telegram.contact

import com.github.kotlintelegrambot.entities.Chat
import ren.natsuyuk1.comet.api.Comet
import ren.natsuyuk1.comet.api.user.User
import ren.natsuyuk1.comet.telegram.TelegramComet
import ren.natsuyuk1.comet.telegram.util.chatID
import ren.natsuyuk1.comet.telegram.util.getDisplayName
import ren.natsuyuk1.comet.telegram.util.send
import ren.natsuyuk1.comet.utils.message.MessageWrapper

abstract class TelegramUser(
    override val id: Long,
    override var name: String,
) : User() {
    override val platformName: String
        get() = "telegram"
}

fun Chat.toCometUser(comet: TelegramComet): TelegramUser {
    val chat = this

    class TelegramUserImpl : TelegramUser(chat.id, chat.getDisplayName()) {
        override val comet: Comet
            get() = comet

        override var card: String = chat.getDisplayName()

        override val id: Long = chat.id

        override val remark: String = chat.getDisplayName()

        override fun sendMessage(message: MessageWrapper) {
            message.send(comet, chat.id.chatID())
        }
    }

    return TelegramUserImpl()
}
