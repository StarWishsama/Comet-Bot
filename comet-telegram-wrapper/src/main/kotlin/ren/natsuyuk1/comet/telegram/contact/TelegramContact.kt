package ren.natsuyuk1.comet.telegram.contact

import dev.inmo.tgbotapi.extensions.utils.chatIdOrNull
import dev.inmo.tgbotapi.types.chat.Chat
import kotlinx.datetime.Clock
import ren.natsuyuk1.comet.api.event.broadcast
import ren.natsuyuk1.comet.api.event.events.comet.MessagePreSendEvent
import ren.natsuyuk1.comet.api.message.MessageReceipt
import ren.natsuyuk1.comet.api.message.MessageSource
import ren.natsuyuk1.comet.api.message.MessageWrapper
import ren.natsuyuk1.comet.api.platform.CometPlatform
import ren.natsuyuk1.comet.api.user.Contact
import ren.natsuyuk1.comet.telegram.TelegramComet
import ren.natsuyuk1.comet.telegram.util.send

internal interface TelegramContact : Contact {
    val contact: Chat

    override val platform: CometPlatform
        get() = CometPlatform.TELEGRAM

    override suspend fun sendMessage(message: MessageWrapper): MessageReceipt? {
        val event = MessagePreSendEvent(
            comet,
            this,
            message,
            Clock.System.now().epochSeconds,
        ).also { it.broadcast() }

        val sourceType =
            when (this) {
                is TelegramGroup -> MessageSource.MessageSourceType.GROUP
                is TelegramGroupMember -> MessageSource.MessageSourceType.BOT
                else -> MessageSource.MessageSourceType.BOT
            }

        return if (!event.isCancelled) {
            contact.id.chatIdOrNull()?.let { chatId ->
                (comet as? TelegramComet)?.send(message, sourceType, chatId)
            }
        } else {
            null
        }
    }
}
