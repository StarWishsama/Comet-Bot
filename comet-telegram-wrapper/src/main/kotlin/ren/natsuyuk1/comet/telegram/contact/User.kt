package ren.natsuyuk1.comet.telegram.contact

import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import ren.natsuyuk1.comet.api.event.broadcast
import ren.natsuyuk1.comet.api.event.events.comet.MessagePreSendEvent
import ren.natsuyuk1.comet.api.platform.LoginPlatform
import ren.natsuyuk1.comet.api.user.User
import ren.natsuyuk1.comet.telegram.TelegramComet
import ren.natsuyuk1.comet.telegram.util.getDisplayName
import ren.natsuyuk1.comet.telegram.util.send
import ren.natsuyuk1.comet.utils.message.MessageWrapper

abstract class TelegramUser(
    override val id: Long,
    override var name: String
) : User() {
    override val platform: LoginPlatform
        get() = LoginPlatform.TELEGRAM
}

class TelegramUserImpl(
    private val from: dev.inmo.tgbotapi.types.chat.User,
    override val comet: TelegramComet
) : TelegramUser(from.id.chatId, from.getDisplayName()) {
    override val id: Long
        get() = from.id.chatId

    override fun sendMessage(message: MessageWrapper) {
        comet.scope.launch {
            val event = MessagePreSendEvent(
                comet,
                this@TelegramUserImpl,
                message,
                Clock.System.now().epochSeconds
            ).also { it.broadcast() }

            if (!event.isCancelled) {
                message.send(comet, from.id)
            }
        }
    }
}

fun dev.inmo.tgbotapi.types.chat.User.toCometUser(comet: TelegramComet): TelegramUser = TelegramUserImpl(this, comet)
