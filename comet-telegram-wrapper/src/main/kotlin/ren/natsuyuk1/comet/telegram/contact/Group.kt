package ren.natsuyuk1.comet.telegram.contact

import dev.inmo.tgbotapi.extensions.api.chat.get.getChatAdministrators
import dev.inmo.tgbotapi.extensions.api.chat.leaveChat
import dev.inmo.tgbotapi.extensions.api.chat.members.getChatMember
import dev.inmo.tgbotapi.extensions.api.chat.modify.setChatTitle
import dev.inmo.tgbotapi.extensions.api.get.getFileAdditionalInfo
import dev.inmo.tgbotapi.extensions.utils.asExtendedGroupChat
import dev.inmo.tgbotapi.extensions.utils.chatIdOrNull
import dev.inmo.tgbotapi.extensions.utils.chatIdOrThrow
import dev.inmo.tgbotapi.requests.abstracts.toInputFile
import dev.inmo.tgbotapi.types.chat.GroupChat
import dev.inmo.tgbotapi.types.chat.member.AdministratorChatMember
import dev.inmo.tgbotapi.types.chat.member.BannedChatMember
import dev.inmo.tgbotapi.types.chat.member.LeftChatMember
import dev.inmo.tgbotapi.types.chat.member.OwnerChatMember
import dev.inmo.tgbotapi.types.files.fullUrl
import dev.inmo.tgbotapi.types.toChatId
import dev.inmo.tgbotapi.utils.PreviewFeature
import ren.natsuyuk1.comet.api.user.Group
import ren.natsuyuk1.comet.api.user.GroupMember
import ren.natsuyuk1.comet.api.user.group.GroupPermission
import ren.natsuyuk1.comet.telegram.TelegramComet

internal class TelegramGroup(
    override val contact: GroupChat,
    override val comet: TelegramComet
) : Group, TelegramContact {
    override val id: Long = contact.id.chatId

    override val name: String = contact.title

    override suspend fun getOwner(): GroupMember = run {
        val resp = comet.bot.getChatAdministrators(contact.id)

        resp.find { it is OwnerChatMember }?.user?.toCometGroupMember(comet, contact.id.chatIdOrThrow())
            ?: error("Unable to retrieve group owner")
    }

    // Telegram couldn't get all members
    override suspend fun getMembers(): List<GroupMember> = emptyList()

    override suspend fun updateGroupName(groupName: String) {
        if (getBotPermission() >= GroupPermission.ADMIN) {
            comet.bot.setChatTitle(contact.id, groupName)
        } else {
            throw IllegalAccessException("Bot doesn't permission to modify group name")
        }
    }

    override suspend fun getBotMuteRemaining(): Int {
        error("You cannot get bot mute remaining time in telegram!")
    }

    override suspend fun getBotPermission(): GroupPermission {
        return when (comet.bot.getChatMember(contact.id, comet.id.toChatId())) {
            is OwnerChatMember -> GroupPermission.OWNER
            is AdministratorChatMember -> GroupPermission.ADMIN
            else -> GroupPermission.MEMBER
        }
    }

    /**
     * 在 Telegram 侧, 获得的头像链接有效期仅有 1 小时
     *
     * 获取后请尽快使用
     *
     */
    @OptIn(PreviewFeature::class)
    override suspend fun getGroupAvatarURL(): String =
        contact.asExtendedGroupChat()?.chatPhoto?.bigFileId?.let {
            val avatarInfo = comet.bot.getFileAdditionalInfo(it.toInputFile())

            return avatarInfo.fullUrl(comet.urlsKeeper)
        } ?: error("指定的群聊必须是 ExtendedGroupChat!")

    override suspend fun getMember(id: Long): GroupMember? {
        return try {
            val resp = comet.bot.getChatMember(contact, id.toChatId())

            contact.id.chatIdOrNull()?.let { resp.user.toCometGroupMember(comet, it) }
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun quit(): Boolean {
        return try {
            comet.bot.leaveChat(id.toChatId())
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun contains(id: Long): Boolean {
        return try {
            val member = comet.bot.getChatMember(id.toChatId(), id.toChatId())

            member !is LeftChatMember && member !is BannedChatMember
        } catch (_: Exception) {
            false
        }
    }
}

fun GroupChat.toCometGroup(comet: TelegramComet): Group = TelegramGroup(this, comet)
