package ren.natsuyuk1.comet.telegram.contact

import dev.inmo.tgbotapi.extensions.api.chat.get.getChatAdministrators
import dev.inmo.tgbotapi.extensions.api.chat.leaveChat
import dev.inmo.tgbotapi.extensions.api.chat.members.getChatMember
import dev.inmo.tgbotapi.extensions.api.chat.modify.setChatTitle
import dev.inmo.tgbotapi.extensions.api.get.getFileAdditionalInfo
import dev.inmo.tgbotapi.extensions.utils.asExtendedGroupChat
import dev.inmo.tgbotapi.extensions.utils.chatIdOrThrow
import dev.inmo.tgbotapi.requests.abstracts.toInputFile
import dev.inmo.tgbotapi.types.chat.GroupChat
import dev.inmo.tgbotapi.types.chat.member.AdministratorChatMember
import dev.inmo.tgbotapi.types.chat.member.OwnerChatMember
import dev.inmo.tgbotapi.types.files.fullUrl
import dev.inmo.tgbotapi.types.toChatId
import dev.inmo.tgbotapi.utils.PreviewFeature
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import ren.natsuyuk1.comet.api.event.broadcast
import ren.natsuyuk1.comet.api.event.events.comet.MessagePreSendEvent
import ren.natsuyuk1.comet.api.message.MessageReceipt
import ren.natsuyuk1.comet.api.message.MessageSource
import ren.natsuyuk1.comet.api.message.MessageWrapper
import ren.natsuyuk1.comet.api.platform.LoginPlatform
import ren.natsuyuk1.comet.api.user.Group
import ren.natsuyuk1.comet.api.user.GroupMember
import ren.natsuyuk1.comet.api.user.group.GroupPermission
import ren.natsuyuk1.comet.telegram.TelegramComet
import ren.natsuyuk1.comet.telegram.util.send

internal abstract class TelegramGroup(
    override val id: Long,
    override var name: String
) : Group(id, name) {
    override val platform: LoginPlatform
        get() = LoginPlatform.TELEGRAM
}

internal class TelegramGroupImpl(
    private val chat: GroupChat,
    override val comet: TelegramComet
) : TelegramGroup(
    chat.id.chatId,
    chat.title
) {
    override val owner: GroupMember
        get() = runBlocking {
            val resp = comet.bot.getChatAdministrators(chat.id)

            resp.find { it is OwnerChatMember }?.user?.toCometGroupMember(comet, chat.id.chatIdOrThrow())
                ?: error("Unable to retrieve group owner")
        }

    // Telegram couldn't get all members
    override val members: List<GroupMember>
        get() = emptyList()

    override fun updateGroupName(groupName: String) {
        comet.scope.launch {
            if (getBotPermission() == GroupPermission.ADMIN) {
                comet.bot.setChatTitle(chat.id, groupName)
            } else {
                throw IllegalAccessException("Bot doesn't permission to modify group name")
            }
        }
    }

    override fun getBotMuteRemaining(): Int {
        error("You cannot get bot mute remaining time in telegram!")
    }

    override fun getBotPermission(): GroupPermission {
        return runBlocking {
            when (comet.bot.getChatMember(chat.id, comet.id.toChatId())) {
                is OwnerChatMember -> GroupPermission.OWNER
                is AdministratorChatMember -> GroupPermission.ADMIN
                else -> GroupPermission.MEMBER
            }
        }
    }

    /**
     * 在 Telegram 侧, 获得的头像链接有效期仅有 1 小时
     *
     * 获取后请尽快使用
     *
     */
    @OptIn(PreviewFeature::class)
    override val avatarUrl: String
        get() {
            chat.asExtendedGroupChat()?.chatPhoto?.bigFileId?.let {
                val avatarInfo = runBlocking {
                    comet.bot.getFileAdditionalInfo(it.toInputFile())
                }

                return avatarInfo.fullUrl(comet.urlsKeeper)
            }

            throw IllegalArgumentException("指定的群聊必须是 ExtendedGroupChat!")
        }

    override fun getMember(id: Long): GroupMember? {
        return runBlocking {
            try {
                val resp = comet.bot.getChatMember(this@TelegramGroupImpl.chat, id.toChatId())

                return@runBlocking resp.user.toCometGroupMember(comet, this@TelegramGroupImpl.chat.id.chatIdOrThrow())
            } catch (e: Exception) {
                return@runBlocking null
            }
        }
    }

    override suspend fun quit(): Boolean {
        return runBlocking {
            try {
                return@runBlocking comet.bot.leaveChat(id.toChatId())
            } catch (e: Exception) {
                return@runBlocking false
            }
        }
    }

    override fun contains(id: Long): Boolean {
        return runBlocking {
            try {
                comet.bot.getChatMember(this@TelegramGroupImpl.id.toChatId(), id.toChatId())
                return@runBlocking true
            } catch (_: Exception) {
                return@runBlocking false
            }
        }
    }

    override suspend fun sendMessage(message: MessageWrapper): MessageReceipt? {
        val event = MessagePreSendEvent(
            comet,
            this@TelegramGroupImpl,
            message,
            Clock.System.now().epochSeconds
        ).also { it.broadcast() }

        return if (!event.isCancelled) {
            comet.send(message, MessageSource.MessageSourceType.GROUP, chat.id.chatIdOrThrow())
        } else {
            null
        }
    }
}

fun GroupChat.toCometGroup(comet: TelegramComet): Group {
    return TelegramGroupImpl(this, comet)
}
