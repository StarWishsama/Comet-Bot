package ren.natsuyuk1.comet.mirai.contact

import kotlinx.datetime.Clock
import ren.natsuyuk1.comet.api.event.broadcast
import ren.natsuyuk1.comet.api.event.events.comet.MessagePreSendEvent
import ren.natsuyuk1.comet.api.message.MessageReceipt
import ren.natsuyuk1.comet.api.message.MessageWrapper
import ren.natsuyuk1.comet.api.platform.CometPlatform
import ren.natsuyuk1.comet.api.user.Contact
import ren.natsuyuk1.comet.mirai.util.toMessageChain
import ren.natsuyuk1.comet.mirai.util.toMessageSource

internal interface MiraiContact : Contact {
    val miraiContact: net.mamoe.mirai.contact.Contact

    override val platform: CometPlatform
        get() = CometPlatform.MIRAI

    override suspend fun sendMessage(message: MessageWrapper): MessageReceipt? {
        val event = MessagePreSendEvent(
            comet,
            this,
            message,
            Clock.System.now().epochSeconds,
        ).also { it.broadcast() }

        return if (!event.isCancelled) {
            val chain = message.toMessageChain(miraiContact).ifEmpty {
                return null
            }

            val receipt = miraiContact.sendMessage(chain)

            MessageReceipt(comet, receipt.source.toMessageSource())
        } else {
            null
        }
    }
}
