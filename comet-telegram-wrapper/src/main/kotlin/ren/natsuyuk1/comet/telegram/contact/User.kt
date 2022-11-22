package ren.natsuyuk1.comet.telegram.contact

import dev.inmo.tgbotapi.extensions.utils.chatIdOrThrow
import kotlinx.datetime.Clock
import ren.natsuyuk1.comet.api.event.broadcast
import ren.natsuyuk1.comet.api.event.events.comet.MessagePreSendEvent
import ren.natsuyuk1.comet.api.message.MessageReceipt
import ren.natsuyuk1.comet.api.message.MessageSource
import ren.natsuyuk1.comet.api.message.MessageWrapper
import ren.natsuyuk1.comet.api.platform.LoginPlatform
import ren.natsuyuk1.comet.api.user.User
import ren.natsuyuk1.comet.telegram.TelegramComet
import ren.natsuyuk1.comet.telegram.util.getDisplayName
import ren.natsuyuk1.comet.telegram.util.send

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

    override suspend fun sendMessage(message: MessageWrapper): MessageReceipt? {
        val event = MessagePreSendEvent(
            comet,
            this@TelegramUserImpl,
            message,
            Clock.System.now().epochSeconds
        ).also { it.broadcast() }

        return if (!event.isCancelled) {
            comet.send(message, MessageSource.MessageSourceType.BOT, from.id.chatIdOrThrow())
        } else null
    }
}

fun dev.inmo.tgbotapi.types.chat.User.toCometUser(comet: TelegramComet): TelegramUser = TelegramUserImpl(this, comet)
