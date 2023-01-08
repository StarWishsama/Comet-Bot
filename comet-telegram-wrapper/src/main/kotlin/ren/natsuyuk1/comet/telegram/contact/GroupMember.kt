package ren.natsuyuk1.comet.telegram.contact

import dev.inmo.tgbotapi.extensions.api.chat.get.getChat
import dev.inmo.tgbotapi.extensions.api.chat.members.banChatMember
import dev.inmo.tgbotapi.extensions.api.chat.members.promoteChatMember
import dev.inmo.tgbotapi.extensions.api.chat.members.restrictChatMember
import dev.inmo.tgbotapi.extensions.utils.asGroupChat
import dev.inmo.tgbotapi.extensions.utils.chatIdOrThrow
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
import ren.natsuyuk1.comet.api.event.broadcast
import ren.natsuyuk1.comet.api.event.events.comet.MessagePreSendEvent
import ren.natsuyuk1.comet.api.message.MessageReceipt
import ren.natsuyuk1.comet.api.message.MessageSource
import ren.natsuyuk1.comet.api.message.MessageWrapper
import ren.natsuyuk1.comet.api.platform.LoginPlatform
import ren.natsuyuk1.comet.api.user.AnonymousMember
import ren.natsuyuk1.comet.api.user.Group
import ren.natsuyuk1.comet.api.user.GroupMember
import ren.natsuyuk1.comet.api.user.group.GroupPermission
import ren.natsuyuk1.comet.telegram.TelegramComet
import ren.natsuyuk1.comet.telegram.util.MUTE
import ren.natsuyuk1.comet.telegram.util.UNMUTE
import ren.natsuyuk1.comet.telegram.util.getDisplayName
import ren.natsuyuk1.comet.telegram.util.send
import kotlin.time.Duration.Companion.seconds

abstract class TelegramGroupMember : GroupMember() {
    abstract val groupID: Long

    override val platform: LoginPlatform = LoginPlatform.TELEGRAM
}

class TelegramGroupUserImpl(
    private val user: User,
    private val groupChatID: Long,
    override val comet: TelegramComet
) : TelegramGroupMember() {
    override val group: Group
        get() = error("Unable to retrieve group when group member is generated from 'User'")

    override val name: String
        get() = user.getDisplayName()
    override var card: String
        get() = name
        set(_) {
            error("Card doesn't exist in telegram platform")
        }

    override suspend fun getGroupPermission(): GroupPermission =
        error("Unable to get permission when group member is generated from 'User'")

    override val id: Long
        get() = user.id.chatId
    override val joinTimestamp: Int
        get() = 0
    override val lastActiveTimestamp: Int
        get() = 0
    override val remainMuteTime: Int
        get() = error("Unable to get remainMuteTime when group member is generated from 'User'")
    override val groupID: Long
        get() = groupChatID

    // When seconds more than 366 days or less than 30 seconds from the current time, they are considered to be restricted forever
    override suspend fun mute(seconds: Int) {
        val triggerTime = Clock.System.now()
        comet.bot.restrictChatMember(
            groupID.toChatId(),
            id.toChatId(),
            TelegramDate((triggerTime + seconds.seconds).epochSeconds),
            MUTE
        )
    }

    override suspend fun unmute() {
        comet.bot.restrictChatMember(groupID.toChatId(), id.toChatId(), permissions = UNMUTE)
    }

    override suspend fun kick(reason: String, block: Boolean) {
        comet.bot.banChatMember(groupChatID.toChatId(), id.toChatId())
    }

    override suspend fun operateAdminPermission(operation: Boolean) {
        if (operation) {
            comet.bot.promoteChatMember(
                chatId = groupChatID.toChatId(),
                userId = id.toChatId(),
                canManageChat = true
            )
        } else {
            comet.bot.promoteChatMember(
                chatId = groupChatID.toChatId(),
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
                canManageChat = null
            )
        }
    }

    override suspend fun sendMessage(message: MessageWrapper): MessageReceipt? {
        val event = MessagePreSendEvent(
            comet,
            this@TelegramGroupUserImpl,
            message,
            Clock.System.now().epochSeconds
        ).also { it.broadcast() }

        return if (!event.isCancelled) {
            comet.send(message, MessageSource.MessageSourceType.GROUP, groupChatID.toChatId().chatIdOrThrow())
        } else {
            null
        }
    }
}

class TelegramGroupMemberImpl(
    private val member: ChatMember,
    private val groupChatID: Long,
    override val comet: TelegramComet
) : TelegramGroupMember() {
    @OptIn(PreviewFeature::class)
    override val group: Group
        get() = runBlocking {
            comet.bot.getChat(groupChatID.toChatId()).asGroupChat()?.toCometGroup(comet)
                ?: error("Unable to retrieve group")
        }

    override val name: String
        get() = member.user.getDisplayName()
    override var card: String
        get() = name
        set(_) {
            error("Card doesn't exist in telegram platform")
        }

    override suspend fun getGroupPermission(): GroupPermission =
        when (member) {
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
        get() = member.user.id.chatId
    override val joinTimestamp: Int
        get() = 0
    override val lastActiveTimestamp: Int
        get() = 0
    override val remainMuteTime: Int
        get() =
            if (member is BannedChatMember && member.untilDate != null) {
                (System.currentTimeMillis() - (member.untilDate!!.asDate.unixMillisLong)).toInt()
            } else {
                -1
            }
    override val groupID: Long
        get() = groupChatID

    // When seconds more than 366 days or less than 30 seconds from the current time, they are considered to be restricted forever
    override suspend fun mute(seconds: Int) {
        val triggerTime = Clock.System.now()
        comet.bot.restrictChatMember(
            groupID.toChatId(),
            id.toChatId(),
            TelegramDate((triggerTime + seconds.seconds).epochSeconds),
            MUTE
        )
    }

    override suspend fun unmute() {
        comet.bot.restrictChatMember(groupID.toChatId(), id.toChatId(), permissions = UNMUTE)
    }

    override suspend fun kick(reason: String, block: Boolean) {
        comet.bot.banChatMember(groupChatID.toChatId(), id.toChatId())
    }

    override suspend fun operateAdminPermission(operation: Boolean) {
        if (operation) {
            comet.bot.promoteChatMember(
                chatId = groupChatID.toChatId(),
                userId = id.toChatId(),
                canManageChat = true
            )
        } else {
            comet.bot.promoteChatMember(
                chatId = groupChatID.toChatId(),
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
                canManageChat = null
            )
        }
    }

    override suspend fun sendMessage(message: MessageWrapper): MessageReceipt? {
        val event = MessagePreSendEvent(
            comet,
            this@TelegramGroupMemberImpl,
            message,
            Clock.System.now().epochSeconds
        ).also { it.broadcast() }

        return if (!event.isCancelled) {
            comet.send(message, MessageSource.MessageSourceType.GROUP, groupChatID.toChatId().chatIdOrThrow())
        } else {
            null
        }
    }
}

fun User.toCometGroupMember(comet: TelegramComet, groupChatID: IdChatIdentifier): GroupMember =
    TelegramGroupUserImpl(this, groupChatID.chatId, comet)

fun ChatMember.toCometGroupMember(comet: TelegramComet, groupChatID: IdChatIdentifier): GroupMember =
    TelegramGroupMemberImpl(this, groupChatID.chatId, comet)

internal class TelegramChannelMemberImpl(
    private val channelChat: ChannelChat,
    override val comet: TelegramComet,
    private val groupChatID: ChatId,
) : AnonymousMember() {
    override val anonymousId: String
        get() = channelChat.id.chatId.toString()

    @OptIn(PreviewFeature::class)
    override val group: Group
        get() = runBlocking {
            comet.bot.getChat(groupChatID).asGroupChat()?.toCometGroup(comet)
                ?: error("Unable to retrieve group")
        }
    override val id: Long
        get() = channelChat.id.chatId
    override val joinTimestamp: Int
        get() = 0
    override val lastActiveTimestamp: Int
        get() = 0
    override val remainMuteTime: Int
        get() = 0
    override val card: String
        get() = channelChat.getDisplayName()

    override suspend fun getGroupPermission(): GroupPermission = GroupPermission.MEMBER

    override suspend fun mute(seconds: Int) {
        val triggerTime = Clock.System.now()
        comet.bot.restrictChatMember(
            groupChatID,
            id.toChatId(),
            TelegramDate((triggerTime + seconds.seconds).epochSeconds),
            MUTE
        )
    }

    override suspend fun unmute() {
        comet.bot.restrictChatMember(groupChatID, id.toChatId(), permissions = UNMUTE)
    }

    override suspend fun kick(reason: String, block: Boolean) {
        comet.bot.banChatMember(groupChatID, id.toChatId())
    }

    override suspend fun operateAdminPermission(operation: Boolean) {
        TODO("Not yet implemented")
    }

    override val name: String
        get() = card
    override val platform: LoginPlatform
        get() = LoginPlatform.TELEGRAM

    override suspend fun sendMessage(message: MessageWrapper): MessageReceipt? {
        error("Anonymous Member cannot send message")
    }
}

fun ChannelChat.toCometAnonymousMember(comet: TelegramComet, groupChatID: ChatId): AnonymousMember =
    TelegramChannelMemberImpl(this, comet, groupChatID)

internal class TelegramGroupAsMemberImpl(
    private val groupChat: GroupChat,
    override val comet: TelegramComet,
) : AnonymousMember() {
    override val anonymousId: String
        get() = groupChat.id.chatId.toString()
    override val group: Group
        get() = groupChat.toCometGroup(comet)
    override val id: Long
        get() = groupChat.id.chatId
    override val joinTimestamp: Int
        get() = 0
    override val lastActiveTimestamp: Int
        get() = 0
    override val remainMuteTime: Int
        get() = TODO("Not yet implemented")
    override val card: String
        get() = groupChat.title

    override suspend fun getGroupPermission(): GroupPermission = GroupPermission.MEMBER

    override suspend fun mute(seconds: Int) {
        val triggerTime = Clock.System.now()
        comet.bot.restrictChatMember(
            groupChat.id,
            id.toChatId(),
            TelegramDate((triggerTime + seconds.seconds).epochSeconds),
            MUTE
        )
    }

    override suspend fun unmute() {
        comet.bot.restrictChatMember(groupChat.id, id.toChatId(), permissions = UNMUTE)
    }

    override suspend fun kick(reason: String, block: Boolean) {
        error("You can't kick 'Group' member")
    }

    override suspend fun operateAdminPermission(operation: Boolean) {
        TODO("Not yet implemented")
    }

    override val name: String
        get() = card
    override val platform: LoginPlatform
        get() = LoginPlatform.TELEGRAM

    override suspend fun sendMessage(message: MessageWrapper): MessageReceipt? {
        error("Anonymous Member cannot send message")
    }
}

fun GroupChat.toCometAnonymousMember(comet: TelegramComet): AnonymousMember =
    TelegramGroupAsMemberImpl(this, comet)
