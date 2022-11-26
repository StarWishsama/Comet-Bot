package ren.natsuyuk1.comet.commands

import ren.natsuyuk1.comet.api.Comet
import ren.natsuyuk1.comet.api.command.CometCommand
import ren.natsuyuk1.comet.api.command.CommandProperty
import ren.natsuyuk1.comet.api.command.PlatformCommandSender
import ren.natsuyuk1.comet.api.command.simpleEquals
import ren.natsuyuk1.comet.api.message.MessageWrapper
import ren.natsuyuk1.comet.api.platform.LoginPlatform
import ren.natsuyuk1.comet.api.session.SessionManager
import ren.natsuyuk1.comet.api.session.VerifySession
import ren.natsuyuk1.comet.api.user.CometUser
import ren.natsuyuk1.comet.util.toMessageWrapper

val START = CommandProperty(
    "start",
    description = "验证命令",
    helpText = "用于验证私聊会话"
)

class StartCommand(
    comet: Comet,
    override val sender: PlatformCommandSender,
    override val subject: PlatformCommandSender,
    val message: MessageWrapper,
    private val user: CometUser
) : CometCommand(comet, sender, subject, message, user, START) {
    override suspend fun run() {
        if (user.platform != LoginPlatform.TELEGRAM) {
            return
        }

        val session = SessionManager.getSession {
            it is VerifySession && (user.uuidEquals(it.cometUser) || subject.simpleEquals(it.contact))
        }

        if (session == null || session !is VerifySession) {
            subject.sendMessage("没有你待处理的会话捏".toMessageWrapper())
            return
        }

        session.verify(sender, message)
    }
}
