package ren.natsuyuk1.comet.telegram.contact

import com.github.kotlintelegrambot.entities.Chat
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import ren.natsuyuk1.comet.api.event.broadcast
import ren.natsuyuk1.comet.api.event.impl.comet.MessageSendEvent
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

class TelegramUserImpl(
    private val chat: Chat,
    override val comet: TelegramComet,
) : TelegramUser(chat.id, chat.getDisplayName()) {
    override var card: String = chat.getDisplayName()

    override val id: Long = chat.id

    override val remark: String = chat.getDisplayName()

    override fun sendMessage(message: MessageWrapper) {
        comet.scope.launch {
            val event = MessageSendEvent(
                comet,
                this@TelegramUserImpl,
                message,
                Clock.System.now().epochSeconds
            ).also { it.broadcast() }

            if (!event.isCancelled)
                message.send(comet, chat.id.chatID())
        }
    }
}

fun Chat.toCometUser(comet: TelegramComet): TelegramUser = TelegramUserImpl(this, comet)
