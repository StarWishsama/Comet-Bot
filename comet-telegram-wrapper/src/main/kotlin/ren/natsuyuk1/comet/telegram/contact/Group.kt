package ren.natsuyuk1.comet.telegram.contact

import com.github.kotlintelegrambot.entities.Chat
import com.github.kotlintelegrambot.entities.ChatMember
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import ren.natsuyuk1.comet.api.event.broadcast
import ren.natsuyuk1.comet.api.event.impl.comet.MessageSendEvent
import ren.natsuyuk1.comet.api.user.Group
import ren.natsuyuk1.comet.api.user.GroupMember
import ren.natsuyuk1.comet.api.user.group.GroupPermission
import ren.natsuyuk1.comet.telegram.TelegramComet
import ren.natsuyuk1.comet.telegram.util.chatID
import ren.natsuyuk1.comet.telegram.util.send
import ren.natsuyuk1.comet.utils.message.MessageWrapper

private val logger = mu.KotlinLogging.logger {}

internal abstract class TelegramGroup(
    override val id: Long,
    override var name: String,
) : Group(id, name) {
    override val platformName: String
        get() = "telegram"
}

internal class TelegramGroupImpl(
    private val chat: Chat,
    override val comet: TelegramComet,
) : TelegramGroup(
    chat.id,
    chat.username ?: "未知群聊",
) {
    override val owner: GroupMember
        get() = run {
            val resp = comet.bot.getChatAdministrators(chat.id.chatID())
            var result: com.github.kotlintelegrambot.entities.User? = null

            resp.fold(
                ifSuccess = { cm ->
                    result = cm.find { it.status == "creator" }?.user
                },
                ifError = {}
            )

            result?.toCometGroupMember(comet, chat.id) ?: error("Unable to retrieve group owner")
        }

    // FIXME
    override val members: List<GroupMember> = listOf()

    override fun updateGroupName(groupName: String) {
        error("You cannot update group name in telegram!")
    }

    override fun getBotMuteRemaining(): Int {
        error("You cannot get bot mute remaining time in telegram!")
    }

    // FIXME: implement true permission
    override fun getBotPermission(): GroupPermission {
        val perms = chat.permissions!!

        return if (perms.canChangeInfo!!) {
            GroupPermission.ADMIN
        } else {
            GroupPermission.MEMBER
        }
    }

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
        val resp = comet.bot.getChatMember(this.id.chatID(), id)
        var cm: ChatMember? = null

        resp.fold(
            ifSuccess = {
                cm = it
            },
            ifError = {
                logger.warn { "获取 Telegram 群 ${this.id} 中的用户时出现意外" }
                return null
            }
        )

        return cm?.user?.toCometGroupMember(comet, this.id)
    }

    override suspend fun quit(): Boolean {
        val (resp, e) = comet.bot.leaveChat(id.chatID())

        if (e != null) {
            logger
            return false
        }

        return resp?.body()?.result ?: false
    }

    override fun contains(id: Long): Boolean {
        val resp = comet.bot.getChatMember(this.id.chatID(), id)

        return resp.isSuccess
    }

    override var card: String
        get() = name
        set(_) {
            error("You cannot set card in telegram!")
        }

    override fun sendMessage(message: MessageWrapper) {

        comet.scope.launch {
            val event = MessageSendEvent(
                comet,
                this@TelegramGroupImpl,
                message,
                Clock.System.now().epochSeconds
            ).also { it.broadcast() }

            if (!event.isCancelled)
                message.send(comet, chat.id.chatID())
        }
    }
}

fun Chat.toCometGroup(comet: TelegramComet): Group {
    if (type != "group" && type != "supergroup") {
        error("Cannot cast a non-group chat to Comet Group")
    }

    return TelegramGroupImpl(this, comet)
}
