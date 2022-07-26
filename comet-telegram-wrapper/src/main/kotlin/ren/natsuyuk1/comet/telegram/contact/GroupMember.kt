package ren.natsuyuk1.comet.telegram.contact

import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.ChatPermissions
import com.github.kotlintelegrambot.entities.User
import kotlinx.datetime.Clock
import ren.natsuyuk1.comet.api.user.GroupMember
import ren.natsuyuk1.comet.telegram.TelegramComet
import ren.natsuyuk1.comet.telegram.util.chatID
import ren.natsuyuk1.comet.telegram.util.send
import ren.natsuyuk1.comet.utils.message.MessageWrapper
import kotlin.time.Duration.Companion.seconds

abstract class TelegramGroupMember : GroupMember() {
    abstract val groupID: Long

    override val platformName: String = "telegram"
}

private val MUTE = ChatPermissions(canSendMessages = false, canSendMediaMessages = false, canSendOtherMessages = false)

private val UNMUTE = ChatPermissions(canSendMessages = true, canSendMediaMessages = true, canSendOtherMessages = true)

class TelegramGroupMemberImpl(
    private val user: User,
    private val groupChatID: Long,
    override val comet: TelegramComet,
) : TelegramGroupMember() {
    override val name: String
        get() = user.firstName + " " + user.lastName
    override var card: String
        get() = name
        set(_) {
            error("You cannot set card in telegram platform")
        }
    override val remark: String
        get() = name
    override val id: Long
        get() = user.id
    override val joinTimestamp: Int
        get() = 0
    override val lastActiveTimestamp: Int
        get() = 0
    override val remainMuteTime: Int
        get() = 0
    override val groupID: Long
        get() = groupChatID

    // When seconds more than 366 days or less than 30 seconds from the current time, they are considered to be restricted forever
    override suspend fun mute(seconds: Int) {
        val triggerTime = Clock.System.now()
        comet.bot.restrictChatMember(ChatId.fromId(groupID), id, MUTE, (triggerTime + seconds.seconds).epochSeconds)
    }

    override suspend fun unmute() {
        comet.bot.restrictChatMember(ChatId.fromId(groupID), id, UNMUTE)
    }

    override suspend fun kick(reason: String, block: Boolean) {
        comet.bot.banChatMember(groupChatID.chatID(), id)
    }

    override suspend fun operateAdminPermission(operation: Boolean) {
        if (operation) comet.bot.promoteChatMember(groupChatID.chatID(), id)
        else comet.bot.promoteChatMember(
            groupChatID.chatID(),
            id,
            isAnonymous = false,
            canChangeInfo = false,
            canPostMessages = false,
            canEditMessages = false,
            canDeleteMessages = false,
            canInviteUsers = false,
            canRestrictMembers = false,
            canPinMessages = false,
            canPromoteMembers = false
        )
    }

    override fun sendMessage(message: MessageWrapper) {
        message.send(comet, groupChatID.chatID())
    }
}

fun User.toCometGroupMember(comet: TelegramComet, groupChatID: Long): GroupMember =
    TelegramGroupMemberImpl(this, groupChatID, comet)
