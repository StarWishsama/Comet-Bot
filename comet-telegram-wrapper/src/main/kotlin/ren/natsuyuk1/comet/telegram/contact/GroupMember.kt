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
import ren.natsuyuk1.comet.api.platform.LoginPlatform
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
    override val chat: User,
    private val groupChatID: ChatId,
    override val comet: TelegramComet
) : GroupMember, TelegramContact {
    @OptIn(PreviewFeature::class)
    override val group: Group
        get() = runBlocking {
            comet.bot.getChat(groupChatID).asGroupChat()?.toCometGroup(comet)
                ?: error("Unable to retrieve group")
        }

    override val name: String
        get() = chat.getDisplayName()
    override var card: String
        get() = name
        set(_) {
            error("Card doesn't exist in telegram platform")
        }

    override suspend fun getGroupPermission(): GroupPermission =
        when (val member = comet.bot.getChatMember(groupChatID.toChatId(), chat)) {
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
        get() = chat.id.chatId

    // When seconds more than 366 days or less than 30 seconds from the current time, they are considered to be restricted forever
    override suspend fun mute(seconds: Int) {
        val triggerTime = Clock.System.now()
        comet.bot.restrictChatMember(
            groupChatID,
            id.toChatId(),
            TelegramDate((triggerTime + seconds.seconds).epochSeconds),
            MUTE
        )
    }

    override suspend fun getJoinTime(): Instant {
        TODO("Not yet implemented")
    }

    override suspend fun getLastActiveTime(): Instant {
        TODO("Not yet implemented")
    }

    override suspend fun getRemainMuteTime(): Duration {
        val member = comet.bot.getChatMember(groupChatID.toChatId(), chat)

        if (member is BannedChatMember && member.untilDate != null) {
            return (System.currentTimeMillis() - (member.untilDate!!.asDate.unixMillisLong)).toInt().seconds
        } else {
            error("This user wasn't be muted")
        }
    }

    override suspend fun unmute() {
        comet.bot.restrictChatMember(groupChatID, id.toChatId(), permissions = UNMUTE)
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
}

fun User.toCometGroupMember(comet: TelegramComet, groupChatID: IdChatIdentifier): GroupMember =
    TelegramGroupMember(this, groupChatID.toChatId(), comet)

internal class TelegramChannelMemberImpl(
    override val chat: ChannelChat,
    override val comet: TelegramComet,
    private val groupChatID: ChatId
) : AnonymousMember, TelegramContact {
    override val anonymousId: String
        get() = chat.id.chatId.toString()

    @OptIn(PreviewFeature::class)
    override val group: Group
        get() = runBlocking {
            comet.bot.getChat(groupChatID).asGroupChat()?.toCometGroup(comet)
                ?: error("Unable to retrieve group")
        }
    override val id: Long
        get() = chat.id.chatId
    override val card: String
        get() = chat.getDisplayName()

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

    override suspend fun getJoinTime(): Instant {
        TODO("Not yet implemented")
    }

    override suspend fun getLastActiveTime(): Instant {
        TODO("Not yet implemented")
    }

    override suspend fun getRemainMuteTime(): Duration {
        TODO("Not yet implemented")
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

internal class TelegramGroupAsMember(
    override val chat: GroupChat,
    override val comet: TelegramComet,
) : AnonymousMember, TelegramContact {
    override val anonymousId: String
        get() = chat.id.chatId.toString()
    override val group: Group
        get() = chat.toCometGroup(comet)
    override val id: Long
        get() = chat.id.chatId
    override val card: String
        get() = chat.title

    override suspend fun getGroupPermission(): GroupPermission = GroupPermission.MEMBER

    override suspend fun mute(seconds: Int) {
        val triggerTime = Clock.System.now()
        comet.bot.restrictChatMember(
            chat.id,
            id.toChatId(),
            TelegramDate((triggerTime + seconds.seconds).epochSeconds),
            MUTE
        )
    }

    override suspend fun getJoinTime(): Instant {
        TODO("Not yet implemented")
    }

    override suspend fun getLastActiveTime(): Instant {
        TODO("Not yet implemented")
    }

    override suspend fun getRemainMuteTime(): Duration {
        TODO("Not yet implemented")
    }

    override suspend fun unmute() {
        comet.bot.restrictChatMember(chat.id, id.toChatId(), permissions = UNMUTE)
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
    TelegramGroupAsMember(this, comet)
