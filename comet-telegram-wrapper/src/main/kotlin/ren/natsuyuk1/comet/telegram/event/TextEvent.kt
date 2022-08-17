package ren.natsuyuk1.comet.telegram.event

import dev.inmo.tgbotapi.abstracts.FromUser
import dev.inmo.tgbotapi.extensions.api.bot.getMe
import dev.inmo.tgbotapi.extensions.utils.extensions.raw.from
import dev.inmo.tgbotapi.extensions.utils.extensions.raw.text
import dev.inmo.tgbotapi.types.chat.GroupChat
import dev.inmo.tgbotapi.types.chat.PrivateChat
import dev.inmo.tgbotapi.types.message.abstracts.CommonMessage
import dev.inmo.tgbotapi.types.message.content.MessageContent
import dev.inmo.tgbotapi.utils.RiskFeature
import mu.KotlinLogging
import ren.natsuyuk1.comet.api.event.impl.message.GroupMessageEvent
import ren.natsuyuk1.comet.api.event.impl.message.MessageEvent
import ren.natsuyuk1.comet.api.event.impl.message.PrivateMessageEvent
import ren.natsuyuk1.comet.telegram.TelegramComet
import ren.natsuyuk1.comet.telegram.contact.toCometGroup
import ren.natsuyuk1.comet.telegram.contact.toCometGroupMember
import ren.natsuyuk1.comet.telegram.contact.toCometUser
import ren.natsuyuk1.comet.telegram.util.getDisplayName
import ren.natsuyuk1.comet.telegram.util.toMessageWrapper

private val logger = KotlinLogging.logger {}

@OptIn(RiskFeature::class)
suspend fun CommonMessage<MessageContent>.toCometEvent(
    comet: TelegramComet
): MessageEvent? {
    if (this !is FromUser) {
        logger.debug { "Incoming chat isn't `FromUser`, is ${this::class.simpleName}" }
        return null
    }

    val isCommand = this.text?.startsWith(comet.bot.getMe().username.username) == true

    return when (chat) {
        is GroupChat -> this.toCometGroupEvent(comet, isCommand)
        is PrivateChat -> this.toCometPrivateEvent(comet, isCommand)
        else -> {
            logger.debug { "Incoming chat group chat or private chat`, is ${chat::class.simpleName}" }
            null
        }
    }
}

@OptIn(RiskFeature::class)
suspend fun CommonMessage<MessageContent>.toCometGroupEvent(
    comet: TelegramComet,
    isCommand: Boolean
): GroupMessageEvent {
    val groupChat = chat as GroupChat

    return GroupMessageEvent(
        comet = comet,
        subject = groupChat.toCometGroup(comet),
        sender = from!!.toCometGroupMember(comet, groupChat.id),
        senderName = from!!.getDisplayName(),
        message = content.toMessageWrapper(comet, isCommand),
        time = date.unixMillisLong,
        messageID = messageId
    )
}

@OptIn(RiskFeature::class)
suspend fun CommonMessage<MessageContent>.toCometPrivateEvent(
    comet: TelegramComet,
    isCommand: Boolean
): PrivateMessageEvent {
    return PrivateMessageEvent(
        comet = comet,
        subject = from!!.toCometUser(comet),
        sender = from!!.toCometUser(comet),
        senderName = from!!.getDisplayName(),
        message = content.toMessageWrapper(comet, isCommand),
        time = date.unixMillisLong,
        messageID = messageId
    )
}
