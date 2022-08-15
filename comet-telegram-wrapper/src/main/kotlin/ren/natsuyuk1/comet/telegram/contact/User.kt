package ren.natsuyuk1.comet.telegram.contact

import dev.inmo.tgbotapi.types.chat.PrivateChat
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import ren.natsuyuk1.comet.api.event.broadcast
import ren.natsuyuk1.comet.api.event.impl.comet.MessagePreSendEvent
import ren.natsuyuk1.comet.api.platform.LoginPlatform
import ren.natsuyuk1.comet.api.user.User
import ren.natsuyuk1.comet.telegram.TelegramComet
import ren.natsuyuk1.comet.telegram.util.getDisplayName
import ren.natsuyuk1.comet.telegram.util.send
import ren.natsuyuk1.comet.utils.message.MessageWrapper

abstract class TelegramUser(
    override val id: Long,
    override var name: String,
) : User() {
    override val platform: LoginPlatform
        get() = LoginPlatform.TELEGRAM
}

class TelegramUserImpl(
    private val chat: PrivateChat,
    override val comet: TelegramComet,
) : TelegramUser(chat.id.chatId, (chat as dev.inmo.tgbotapi.types.chat.User).getDisplayName()) {
    override var card: String = (chat as dev.inmo.tgbotapi.types.chat.User).getDisplayName()

    override val id: Long = chat.id.chatId

    override val remark: String = (chat as dev.inmo.tgbotapi.types.chat.User).getDisplayName()

    override fun sendMessage(message: MessageWrapper) {
        comet.scope.launch {
            val event = MessagePreSendEvent(
                comet,
                this@TelegramUserImpl,
                message,
                Clock.System.now().epochSeconds
            ).also { it.broadcast() }

            if (!event.isCancelled)
                message.send(comet, chat.id)
        }
    }
}

fun PrivateChat.toCometUser(comet: TelegramComet): TelegramUser = TelegramUserImpl(this, comet)
