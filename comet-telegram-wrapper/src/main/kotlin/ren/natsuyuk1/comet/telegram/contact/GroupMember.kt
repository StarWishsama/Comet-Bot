package ren.natsuyuk1.comet.telegram.contact

import dev.inmo.tgbotapi.extensions.api.chat.get.getChat
import dev.inmo.tgbotapi.extensions.api.chat.members.banChatMember
import dev.inmo.tgbotapi.extensions.api.chat.members.getChatMember
import dev.inmo.tgbotapi.extensions.api.chat.members.promoteChatMember
import dev.inmo.tgbotapi.extensions.api.chat.members.restrictChatMember
import dev.inmo.tgbotapi.extensions.utils.asGroupChat
import dev.inmo.tgbotapi.types.ChatId
import dev.inmo.tgbotapi.types.IdChatIdentifier
import dev.inmo.tgbotapi.types.TelegramDate
import dev.inmo.tgbotapi.types.chat.ChannelChat
import dev.inmo.tgbotapi.types.chat.GroupChat
import dev.inmo.tgbotapi.types.chat.User
import dev.inmo.tgbotapi.types.chat.member.*
import dev.inmo.tgbotapi.types.toChatId
import dev.inmo.tgbotapi.utils.PreviewFeature
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import ren.natsuyuk1.comet.api.message.MessageReceipt
import ren.natsuyuk1.comet.api.message.MessageWrapper
import ren.natsuyuk1.comet.api.platform.CometPlatform
import ren.natsuyuk1.comet.api.user.AnonymousMember
import ren.natsuyuk1.comet.api.user.Group
import ren.natsuyuk1.comet.api.user.GroupMember
import ren.natsuyuk1.comet.api.user.group.GroupPermission
import ren.natsuyuk1.comet.telegram.TelegramComet
import ren.natsuyuk1.comet.telegram.util.MUTE
import ren.natsuyuk1.comet.telegram.util.UNMUTE
import ren.natsuyuk1.comet.telegram.util.getDisplayName
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class TelegramGroupMember(
    override val contact: User,
    private val groupId: ChatId,
    override val comet: TelegramComet,
) : GroupMember, TelegramContact {
    @OptIn(PreviewFeature::class)
    override val group: Group
        get() = runBlocking {
            comet.bot.getChat(groupId).asGroupChat()?.toCometGroup(comet)
                ?: error("Unable to retrieve group")
        }

    override val name: String
        get() = contact.getDisplayName()
    override var card: String
        get() = name
        set(_) {
            error("Card doesn't exist in telegram platform")
        }

    override suspend fun getGroupPermission(): GroupPermission =
        when (val member = comet.bot.getChatMember(groupId.toChatId(), contact)) {
            is AdministratorChatMember -> {
                if (member is OwnerChatMember) {
                    GroupPermission.OWNER
                } else {
                    GroupPermission.ADMIN
                }
            }

            else -> {
                GroupPermission.MEMBER
            }
        }

    override val id: Long
        get() = contact.id.chatId

    // When seconds more than 366 days or less than 30 seconds from the current time, they are considered to be restricted forever
    override suspend fun mute(seconds: Int) {
        val triggerTime = Clock.System.now()
        comet.bot.restrictChatMember(
            groupId,
            id.toChatId(),
            TelegramDate((triggerTime + seconds.seconds).epochSeconds),
            MUTE,
        )
    }

    override suspend fun getJoinTime(): Instant = error("Unable to get join time in telegram")

    override suspend fun getLastActiveTime(): Instant = error("Unable to get last active time in telegram")

    override suspend fun getRemainMuteTime(): Duration {
        val member = comet.bot.getChatMember(groupId.toChatId(), contact)

        if (member is BannedChatMember && member.untilDate != null) {
            return (System.currentTimeMillis() - (member.untilDate!!.asDate.unixMillisLong)).toInt().seconds
        } else {
            error("This user wasn't be muted")
        }
    }

    override suspend fun unmute() {
        comet.bot.restrictChatMember(groupId, id.toChatId(), permissions = UNMUTE)
    }

    override suspend fun kick(reason: String, block: Boolean) {
        comet.bot.banChatMember(groupId.toChatId(), id.toChatId())
    }

    override suspend fun operateAdminPermission(operation: Boolean) {
        if (operation) {
            comet.bot.promoteChatMember(
                chatId = groupId.toChatId(),
                userId = id.toChatId(),
                canManageChat = true,
            )
        } else {
            comet.bot.promoteChatMember(
                chatId = groupId.toChatId(),
                userId = id.toChatId(),
                isAnonymous = false,
                canChangeInfo = false,
                canPostMessages = false,
                canEditMessages = false,
                canDeleteMessages = false,
                canInviteUsers = false,
                canRestrictMembers = false,
                canPinMessages = false,
                canPromoteMembers = false,
                canManageChat = null,
            )
        }
    }
}

fun User.toCometGroupMember(comet: TelegramComet, subject: IdChatIdentifier): GroupMember =
    TelegramGroupMember(this, subject.toChatId(), comet)

internal class TelegramChannelMemberImpl(
    override val contact: ChannelChat,
    override val comet: TelegramComet,
    private val groupId: ChatId,
) : AnonymousMember, TelegramContact {
    override val anonymousId: String
        get() = contact.id.chatId.toString()

    @OptIn(PreviewFeature::class)
    override val group: Group
        get() = runBlocking {
            comet.bot.getChat(groupId).asGroupChat()?.toCometGroup(comet)
                ?: error("Unable to retrieve group")
        }
    override val id: Long
        get() = contact.id.chatId
    override val card: String
        get() = contact.getDisplayName()

    override suspend fun getGroupPermission(): GroupPermission = GroupPermission.MEMBER

    override suspend fun mute(seconds: Int) {
        val triggerTime = Clock.System.now()
        comet.bot.restrictChatMember(
            groupId,
            id.toChatId(),
            TelegramDate((triggerTime + seconds.seconds).epochSeconds),
            MUTE,
        )
    }

    override suspend fun getJoinTime(): Instant = error("Unable to get join time in telegram")

    override suspend fun getLastActiveTime(): Instant = error("Unable to get last active time in telegram")

    override suspend fun getRemainMuteTime(): Duration {
        val member = comet.bot.getChatMember(groupId.toChatId(), contact.id)

        if (member is BannedChatMember && member.untilDate != null) {
            return (System.currentTimeMillis() - (member.untilDate!!.asDate.unixMillisLong)).toInt().seconds
        } else {
            error("This user wasn't be muted")
        }
    }

    override suspend fun unmute() {
        comet.bot.restrictChatMember(groupId, id.toChatId(), permissions = UNMUTE)
    }

    override suspend fun kick(reason: String, block: Boolean) {
        comet.bot.banChatMember(groupId, id.toChatId())
    }

    override suspend fun operateAdminPermission(operation: Boolean) {
        if (operation) {
            comet.bot.promoteChatMember(
                chatId = groupId.toChatId(),
                userId = id.toChatId(),
                canManageChat = true,
            )
        } else {
            comet.bot.promoteChatMember(
                chatId = groupId.toChatId(),
                userId = id.toChatId(),
                isAnonymous = false,
                canChangeInfo = false,
                canPostMessages = false,
                canEditMessages = false,
                canDeleteMessages = false,
                canInviteUsers = false,
                canRestrictMembers = false,
                canPinMessages = false,
                canPromoteMembers = false,
                canManageChat = null,
            )
        }
    }

    override val name: String
        get() = card
    override val platform: CometPlatform
        get() = CometPlatform.TELEGRAM

    override suspend fun sendMessage(message: MessageWrapper): MessageReceipt? {
        error("Anonymous Member cannot send message")
    }
}

fun ChannelChat.toCometAnonymousMember(comet: TelegramComet, subject: ChatId): AnonymousMember =
    TelegramChannelMemberImpl(this, comet, subject)

internal class TelegramGroupAsMember(
    override val contact: GroupChat,
    override val comet: TelegramComet,
) : AnonymousMember, TelegramContact {
    override val anonymousId: String
        get() = contact.id.chatId.toString()
    override val group: Group
        get() = contact.toCometGroup(comet)
    override val id: Long
        get() = contact.id.chatId
    override val name: String
        get() = card
    override val card: String
        get() = contact.title

    override suspend fun getGroupPermission(): GroupPermission = GroupPermission.MEMBER

    override suspend fun mute(seconds: Int) {
        val triggerTime = Clock.System.now()
        comet.bot.restrictChatMember(
            contact.id,
            id.toChatId(),
            TelegramDate((triggerTime + seconds.seconds).epochSeconds),
            MUTE,
        )
    }

    override suspend fun getJoinTime(): Instant = error("Unable to get last join time from group account")

    override suspend fun getLastActiveTime(): Instant = error("Unable to get last active time from group account")

    override suspend fun getRemainMuteTime(): Duration = error("Unable to get remain mute time from group account")

    override suspend fun unmute() {
        comet.bot.restrictChatMember(contact.id, id.toChatId(), permissions = UNMUTE)
    }

    override suspend fun kick(reason: String, block: Boolean) {
        error("You can't kick group account")
    }

    override suspend fun operateAdminPermission(operation: Boolean) {
        error("You can't operate permission of group account")
    }

    override suspend fun sendMessage(message: MessageWrapper): MessageReceipt? {
        error("Anonymous Member cannot send message")
    }
}

fun GroupChat.toCometAnonymousMember(comet: TelegramComet): AnonymousMember =
    TelegramGroupAsMember(this, comet)
