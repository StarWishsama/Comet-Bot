package ren.natsuyuk1.comet.telegram.contact

import dev.inmo.tgbotapi.extensions.api.chat.members.banChatMember
import dev.inmo.tgbotapi.extensions.api.chat.members.getChatMember
import dev.inmo.tgbotapi.extensions.api.chat.members.promoteChatMember
import dev.inmo.tgbotapi.extensions.api.chat.members.restrictChatMember
import dev.inmo.tgbotapi.types.ChatId
import dev.inmo.tgbotapi.types.TelegramDate
import dev.inmo.tgbotapi.types.chat.ChatPermissions
import dev.inmo.tgbotapi.types.chat.User
import dev.inmo.tgbotapi.types.chat.member.BannedChatMember
import dev.inmo.tgbotapi.types.toChatId
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import ren.natsuyuk1.comet.api.event.broadcast
import ren.natsuyuk1.comet.api.event.impl.comet.MessagePreSendEvent
import ren.natsuyuk1.comet.api.platform.LoginPlatform
import ren.natsuyuk1.comet.api.user.GroupMember
import ren.natsuyuk1.comet.telegram.TelegramComet
import ren.natsuyuk1.comet.telegram.util.getDisplayName
import ren.natsuyuk1.comet.telegram.util.send
import ren.natsuyuk1.comet.utils.message.MessageWrapper
import kotlin.time.Duration.Companion.seconds

abstract class TelegramGroupMember : GroupMember() {
    abstract val groupID: Long

    override val platform: LoginPlatform = LoginPlatform.TELEGRAM
}

private val MUTE = ChatPermissions(canSendMessages = false, canSendMediaMessages = false, canSendOtherMessages = false)

private val UNMUTE = ChatPermissions(canSendMessages = true, canSendMediaMessages = true, canSendOtherMessages = true)

class TelegramGroupMemberImpl(
    private val user: User,
    private val groupChatID: Long,
    override val comet: TelegramComet,
) : TelegramGroupMember() {
    override val name: String
        get() = user.getDisplayName()
    override var card: String
        get() = name
        set(_) {
            error("Card doesn't exist in telegram platform")
        }
    override val remark: String
        get() = name
    override val id: Long
        get() = user.id.chatId
    override val joinTimestamp: Int
        get() = 0
    override val lastActiveTimestamp: Int
        get() = 0
    override val remainMuteTime: Int
        get() = runBlocking<Int> {
            val resp = comet.bot.getChatMember(groupChatID.toChatId(), user)

            if (resp is BannedChatMember) {
                (System.currentTimeMillis() - (resp.untilDate?.asDate?.unixMillisLong ?: return@runBlocking -1)).toInt()
            } else {
                -1
            }
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
        if (operation) comet.bot.promoteChatMember(
            chatId = groupChatID.toChatId(),
            userId = id.toChatId(),
            canManageChat = true
        )
        else comet.bot.promoteChatMember(
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

    override fun sendMessage(message: MessageWrapper) {
        comet.scope.launch {
            val event = MessagePreSendEvent(
                comet,
                this@TelegramGroupMemberImpl,
                message,
                Clock.System.now().epochSeconds
            ).also { it.broadcast() }

            if (!event.isCancelled)
                message.send(comet, groupChatID.toChatId())
        }
    }
}

fun User.toCometGroupMember(comet: TelegramComet, groupChatID: ChatId): GroupMember =
    TelegramGroupMemberImpl(this, groupChatID.chatId, comet)
