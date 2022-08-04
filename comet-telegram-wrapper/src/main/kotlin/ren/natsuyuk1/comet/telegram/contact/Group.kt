package ren.natsuyuk1.comet.telegram.contact

import com.github.kotlintelegrambot.entities.Chat
import com.github.kotlintelegrambot.entities.ChatMember
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import ren.natsuyuk1.comet.api.event.broadcast
import ren.natsuyuk1.comet.api.event.impl.comet.MessagePreSendEvent
import ren.natsuyuk1.comet.api.platform.LoginPlatform
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
    override val platform: LoginPlatform
        get() = LoginPlatform.TELEGRAM
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

    // Telegram couldn't get all members
    override val members: List<GroupMember> = emptyList()

    override fun updateGroupName(groupName: String) {
        if (getBotPermission() == GroupPermission.ADMIN) {
            comet.bot.setChatTitle(chat.id.chatID(), groupName)
        } else {
            throw IllegalAccessException("Bot doesn't permission to modify group name")
        }
    }

    override fun getBotMuteRemaining(): Int {
        error("You cannot get bot mute remaining time in telegram!")
    }

    override fun getBotPermission(): GroupPermission {
        val cm = comet.bot.getChatMember(chat.id.chatID(), comet.id)

        return if (cm.isSuccess) {
            if (cm.get().status == "administrator" || cm.get().status == "creator") GroupPermission.ADMIN else GroupPermission.MEMBER
        } else {
            GroupPermission.MEMBER
        }
    }

    /**
     * 在 Telegram 侧, 获得的头像链接有效期仅有 1 小时
     *
     * 获取后请尽快使用
     */
    override val avatarUrl: String
        get() = run<String> {
            val bigFileId = chat.photo?.bigFileId ?: return@run ""

            val (resp, _) = comet.bot.getFile(bigFileId)

            if (resp?.isSuccessful == true) {
                return@run "https://api.telegram.org/file/bot${comet.config.password}/${resp.body()?.result?.fileId}"
            } else {
                return@run ""
            }
        }

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
        get() = ""
        set(_) {
            error("You cannot set card in telegram!")
        }

    override fun sendMessage(message: MessageWrapper) {
        comet.scope.launch {
            val event = MessagePreSendEvent(
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
