package ren.natsuyuk1.comet.telegram.event

import com.github.kotlintelegrambot.dispatcher.handlers.MessageHandlerEnvironment
import ren.natsuyuk1.comet.api.event.impl.message.GroupMessageEvent
import ren.natsuyuk1.comet.api.event.impl.message.MessageEvent
import ren.natsuyuk1.comet.api.event.impl.message.PrivateMessageEvent
import ren.natsuyuk1.comet.telegram.TelegramComet
import ren.natsuyuk1.comet.telegram.contact.toCometGroup
import ren.natsuyuk1.comet.telegram.contact.toCometGroupMember
import ren.natsuyuk1.comet.telegram.contact.toCometUser
import ren.natsuyuk1.comet.telegram.util.toMessageWrapper

suspend fun MessageHandlerEnvironment.toCometEvent(comet: TelegramComet): MessageEvent? {
    return when (message.chat.type) {
        "group", "supergroup" -> this.toCometGroupEvent(comet)
        "private" -> this.toCometPrivateEvent(comet)
        else -> null
    }
}

suspend fun MessageHandlerEnvironment.toCometGroupEvent(comet: TelegramComet): GroupMessageEvent {
    return GroupMessageEvent(
        comet = comet,
        subject = this.message.chat.toCometGroup(comet),
        sender = this.message.from!!.toCometGroupMember(comet, this.message.chat.id),
        senderName = this.message.from?.firstName + " " + this.message.from?.lastName,
        message = this.message.toMessageWrapper(comet),
        time = this.message.date,
        messageID = this.message.messageId
    )
}

suspend fun MessageHandlerEnvironment.toCometPrivateEvent(comet: TelegramComet): PrivateMessageEvent {
    return PrivateMessageEvent(
        comet = comet,
        subject = this.message.chat.toCometUser(comet),
        sender = this.message.chat.toCometUser(comet),
        senderName = this.message.from?.firstName + " " + this.message.from?.lastName,
        message = this.message.toMessageWrapper(comet),
        time = this.message.date,
        messageID = this.message.messageId
    )
}
