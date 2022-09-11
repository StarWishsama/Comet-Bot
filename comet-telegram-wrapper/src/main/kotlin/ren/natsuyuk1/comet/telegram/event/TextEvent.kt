package ren.natsuyuk1.comet.telegram.event

import dev.inmo.tgbotapi.abstracts.FromUser
import dev.inmo.tgbotapi.extensions.api.bot.getMe
import dev.inmo.tgbotapi.extensions.utils.extensions.raw.entities
import dev.inmo.tgbotapi.extensions.utils.extensions.raw.from
import dev.inmo.tgbotapi.extensions.utils.fromChannelGroupContentMessageOrNull
import dev.inmo.tgbotapi.types.chat.GroupChat
import dev.inmo.tgbotapi.types.chat.PrivateChat
import dev.inmo.tgbotapi.types.message.abstracts.AnonymousGroupContentMessage
import dev.inmo.tgbotapi.types.message.abstracts.CommonMessage
import dev.inmo.tgbotapi.types.message.abstracts.WithSenderChatMessage
import dev.inmo.tgbotapi.types.message.content.MessageContent
import dev.inmo.tgbotapi.types.message.textsources.BotCommandTextSource
import dev.inmo.tgbotapi.utils.RiskFeature
import mu.KotlinLogging
import ren.natsuyuk1.comet.api.event.events.message.GroupMessageEvent
import ren.natsuyuk1.comet.api.event.events.message.MessageEvent
import ren.natsuyuk1.comet.api.event.events.message.PrivateMessageEvent
import ren.natsuyuk1.comet.telegram.TelegramComet
import ren.natsuyuk1.comet.telegram.contact.toCometAnonymousMember
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
    if (this !is FromUser && this !is WithSenderChatMessage) {
        logger.debug { "Incoming message doesn't have user or sender, is ${this::class.simpleName}" }
        return null
    }

    val botName = comet.bot.getMe().username.username
    val isCommand = this.entities?.find { it is BotCommandTextSource && it.source.contains(botName) } != null

    return when (chat) {
        is GroupChat -> this.toCometGroupEvent(comet, isCommand)
        is PrivateChat -> this.toCometPrivateEvent(comet, isCommand)
        else -> {
            logger.debug { "Incoming chat isn't group chat or private chat`, is ${chat::class.simpleName}" }
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

    val channelGroupMsg = fromChannelGroupContentMessageOrNull()

    return when {
        channelGroupMsg != null -> {
            val channelSender = channelGroupMsg.channel.toCometAnonymousMember(comet, groupChat.id)
            GroupMessageEvent(
                comet = comet,
                subject = groupChat.toCometGroup(comet),
                sender = channelSender,
                senderName = channelGroupMsg.channel.getDisplayName(),
                message = content.toMessageWrapper(channelSender.id, groupChat.id.chatId, date.unixMillisLong, messageId, comet, isCommand),
                time = date.unixMillisLong,
                messageID = messageId
            )
        }
        this is AnonymousGroupContentMessage -> {
            val anonymousSender = senderChat.toCometAnonymousMember(comet)
            GroupMessageEvent(
                comet = comet,
                subject = groupChat.toCometGroup(comet),
                sender = anonymousSender,
                senderName = senderChat.title,
                message = content.toMessageWrapper(anonymousSender.id, groupChat.id.chatId, date.unixMillisLong, messageId, comet, isCommand),
                time = date.unixMillisLong,
                messageID = messageId
            )
        }
        else -> {
            val sender = from!!.toCometGroupMember(comet, groupChat.id)

            GroupMessageEvent(
                comet = comet,
                subject = groupChat.toCometGroup(comet),
                sender = sender,
                senderName = from!!.getDisplayName(),
                message = content.toMessageWrapper(sender.id, groupChat.id.chatId, date.unixMillisLong, messageId, comet, isCommand),
                time = date.unixMillisLong,
                messageID = messageId
            )
        }
    }
}

@OptIn(RiskFeature::class)
suspend fun CommonMessage<MessageContent>.toCometPrivateEvent(
    comet: TelegramComet,
    isCommand: Boolean
): PrivateMessageEvent {
    val sender = from!!.toCometUser(comet)

    return PrivateMessageEvent(
        comet = comet,
        subject = sender,
        sender = sender,
        senderName = from!!.getDisplayName(),
        message = content.toMessageWrapper(sender.id, comet.id, date.unixMillisLong, messageId, comet, isCommand),
        time = date.unixMillisLong,
        messageID = messageId
    )
}
