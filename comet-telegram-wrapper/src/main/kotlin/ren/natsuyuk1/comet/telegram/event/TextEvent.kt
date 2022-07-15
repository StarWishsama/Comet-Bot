package ren.natsuyuk1.comet.telegram.event

import com.github.kotlintelegrambot.dispatcher.handlers.TextHandlerEnvironment
import ren.natsuyuk1.comet.api.event.impl.message.GroupMessageEvent
import ren.natsuyuk1.comet.api.event.impl.message.MessageEvent
import ren.natsuyuk1.comet.telegram.TelegramComet
import ren.natsuyuk1.comet.telegram.contact.toCometGroup
import ren.natsuyuk1.comet.telegram.contact.toCometGroupMember
import ren.natsuyuk1.comet.telegram.util.toMessageWrapper

suspend fun TextHandlerEnvironment.toCometEvent(comet: TelegramComet): MessageEvent? {
    return when (message.chat.type) {
        "group", "supergroup" -> this.toCometGroupEvent(comet)
        else -> null
    }
}

suspend fun TextHandlerEnvironment.toCometGroupEvent(comet: TelegramComet): GroupMessageEvent {
    return GroupMessageEvent(
        comet = comet,
        subject = this.toCometGroup(comet),
        sender = this.message.from!!.toCometGroupMember(comet, this.message.chat.id),
        senderName = this.message.from?.firstName + this.message.from?.lastName,
        message = this.message.toMessageWrapper(comet),
        time = this.message.date,
        messageID = this.message.messageId
    )
}
