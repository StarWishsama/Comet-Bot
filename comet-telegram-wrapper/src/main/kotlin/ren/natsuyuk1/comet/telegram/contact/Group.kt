package ren.natsuyuk1.comet.telegram.contact

import com.github.kotlintelegrambot.dispatcher.handlers.TextHandlerEnvironment
import ren.natsuyuk1.comet.api.Comet
import ren.natsuyuk1.comet.api.user.Group
import ren.natsuyuk1.comet.api.user.GroupMember
import ren.natsuyuk1.comet.api.user.group.GroupPermission
import ren.natsuyuk1.comet.telegram.TelegramComet
import ren.natsuyuk1.comet.telegram.util.chatID
import ren.natsuyuk1.comet.telegram.util.send
import ren.natsuyuk1.comet.utils.message.MessageWrapper

abstract class TelegramGroup(
    override val id: Long,
    override var name: String,
    override val owner: GroupMember,
    override val members: List<GroupMember>,
) : Group(id, name, owner, members) {
    override val platformName: String
        get() = "telegram"
}

fun TextHandlerEnvironment.toCometGroup(comet: TelegramComet): Group {
    if (message.chat.type != "group" && message.chat.type != "supergroup") {
        error("Cannot cast a non-group chat to Comet Group")
    }

    val chat = message.chat

    class TelegramGroupImpl : TelegramGroup(
        chat.id,
        chat.username ?: "未知群聊",
        message.from?.toCometGroupMember(comet, message.chat.id)!!,
        members = listOf()
    ) {
        override fun updateGroupName(groupName: String) {
            error("You cannot update group name in telegram!")
        }

        override fun getBotMuteRemaining(): Int {
            error("You cannot get bot mute remaining time in telegram!")
        }

        // FIXME: implement true permission
        override fun getBotPermission(): GroupPermission = GroupPermission.MEMBER

        /**
         * 在 Telegram 侧, 你只能获得对应头像的 fileId
         *
         * 你需要通过 comet.bot.getFile(...) 获取一个文件的 ID
         *
         * 并调用 https://api.telegram.org/file/bot<token>/<file_id> 下载
         */
        override val avatarUrl: String
            get() = chat.photo?.bigFileId ?: ""

        override fun getMember(id: Long): GroupMember? {
            TODO("Not yet implemented")
        }

        override suspend fun quit(): Boolean {
            TODO("Not yet implemented")
        }

        override fun contains(id: Long): Boolean {
            TODO("Not yet implemented")
        }

        override val comet: Comet
            get() = comet
        override var card: String
            get() = name
            set(_) {
                error("You cannot set card in telegram!")
            }

        override fun sendMessage(message: MessageWrapper) {
            message.send(comet, this@toCometGroup.message.chat.id.chatID())
        }
    }

    return TelegramGroupImpl()
}
