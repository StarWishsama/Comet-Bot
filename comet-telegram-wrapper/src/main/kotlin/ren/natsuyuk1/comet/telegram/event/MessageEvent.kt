package ren.natsuyuk1.comet.telegram.event

import dev.inmo.tgbotapi.abstracts.FromUser
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onContentMessage
import dev.inmo.tgbotapi.extensions.utils.asCommonGroupContentMessage
import dev.inmo.tgbotapi.extensions.utils.asPrivateContentMessage
import dev.inmo.tgbotapi.extensions.utils.chatIdOrThrow
import dev.inmo.tgbotapi.extensions.utils.extensions.raw.entities
import dev.inmo.tgbotapi.types.chat.GroupChat
import dev.inmo.tgbotapi.types.chat.PrivateChat
import dev.inmo.tgbotapi.types.message.abstracts.*
import dev.inmo.tgbotapi.types.message.content.MessageContent
import dev.inmo.tgbotapi.types.message.textsources.BotCommandTextSource
import dev.inmo.tgbotapi.types.toChatId
import dev.inmo.tgbotapi.utils.PreviewFeature
import dev.inmo.tgbotapi.utils.RiskFeature
import kotlinx.coroutines.launch
import mu.KotlinLogging
import ren.natsuyuk1.comet.api.event.broadcast
import ren.natsuyuk1.comet.api.event.events.message.GroupMessageEvent
import ren.natsuyuk1.comet.api.event.events.message.MessageEvent
import ren.natsuyuk1.comet.api.event.events.message.PrivateMessageEvent
import ren.natsuyuk1.comet.api.message.MessageSource
import ren.natsuyuk1.comet.telegram.TelegramComet
import ren.natsuyuk1.comet.telegram.contact.toCometAnonymousMember
import ren.natsuyuk1.comet.telegram.contact.toCometGroup
import ren.natsuyuk1.comet.telegram.contact.toCometGroupMember
import ren.natsuyuk1.comet.telegram.contact.toCometUser
import ren.natsuyuk1.comet.telegram.util.format
import ren.natsuyuk1.comet.telegram.util.getDisplayName
import ren.natsuyuk1.comet.telegram.util.toMessageWrapper

private val logger = KotlinLogging.logger {}

suspend fun BehaviourContext.listenMessageEvent(comet: TelegramComet) {
    onContentMessage(
        { it.chat is PrivateChat || it.chat is GroupChat }
    ) {
        if (it.date < comet.startTime) {
            return@onContentMessage
        }

        logger.trace { it.format() }

        scope.launch {
            it.toCometEvent(comet)?.broadcast()
        }
    }
}

@OptIn(PreviewFeature::class, RiskFeature::class)
suspend fun CommonMessage<MessageContent>.toCometEvent(
    comet: TelegramComet
): MessageEvent? {
    if (this !is FromUser && this !is WithSenderChatMessage) {
        return null
    }

    val containAt = entities?.any { it is BotCommandTextSource && it.source.contains(comet.username) } == true

    return when (chat) {
        is GroupChat -> this.asCommonGroupContentMessage()?.toCometGroupEvent(comet, containAt)
        is PrivateChat -> this.asPrivateContentMessage()?.toCometPrivateEvent(comet, containAt)
        else -> null
    }
}

suspend fun CommonGroupContentMessage<MessageContent>.toCometGroupEvent(
    comet: TelegramComet,
    isCommand: Boolean
): GroupMessageEvent {
    return when (this) {
        is FromChannelGroupContentMessage<*> -> {
            val channelSender = channel.toCometAnonymousMember(comet, chat.id.toChatId())

            GroupMessageEvent(
                comet = comet,
                subject = chat.toCometGroup(comet),
                sender = channelSender,
                senderName = channel.getDisplayName(),
                message = content.toMessageWrapper(
                    type = MessageSource.MessageSourceType.GROUP,
                    from = channelSender.id,
                    to = chat.id.chatId,
                    time = date.unixMillisLong,
                    msgID = messageId,
                    comet = comet,
                    containBotAt = isCommand
                ),
                time = date.unixMillisLong,
                messageID = messageId
            )
        }

        is AnonymousGroupContentMessage<*> -> {
            val anonymousSender = senderChat.toCometAnonymousMember(comet)

            GroupMessageEvent(
                comet = comet,
                subject = chat.toCometGroup(comet),
                sender = anonymousSender,
                senderName = senderChat.title,
                message = content.toMessageWrapper(
                    type = MessageSource.MessageSourceType.GROUP,
                    from = anonymousSender.id,
                    to = chat.id.chatId,
                    time = date.unixMillisLong,
                    msgID = messageId,
                    comet = comet,
                    containBotAt = isCommand
                ),
                time = date.unixMillisLong,
                messageID = messageId
            )
        }

        else -> {
            val sender = from.toCometGroupMember(comet, chat.id.chatIdOrThrow())

            GroupMessageEvent(
                comet = comet,
                subject = chat.toCometGroup(comet),
                sender = sender,
                senderName = from.getDisplayName(),
                message = content.toMessageWrapper(
                    type = MessageSource.MessageSourceType.GROUP,
                    from = sender.id,
                    to = chat.id.chatId,
                    time = date.unixMillisLong,
                    msgID = messageId,
                    comet = comet,
                    containBotAt = isCommand
                ),
                time = date.unixMillisLong,
                messageID = messageId
            )
        }
    }
}

suspend fun PrivateContentMessage<MessageContent>.toCometPrivateEvent(
    comet: TelegramComet,
    containAt: Boolean
): PrivateMessageEvent {
    val sender = from.toCometUser(comet)

    return PrivateMessageEvent(
        comet = comet,
        subject = sender,
        sender = sender,
        senderName = from.getDisplayName(),
        message = content.toMessageWrapper(
            type = MessageSource.MessageSourceType.BOT,
            from = sender.id,
            to = comet.id,
            time = date.unixMillisLong,
            msgID = messageId,
            comet = comet,
            containBotAt = containAt
        ),
        time = date.unixMillisLong,
        messageID = messageId
    )
}
