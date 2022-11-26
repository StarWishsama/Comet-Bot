package ren.natsuyuk1.comet.commands

import ren.natsuyuk1.comet.api.Comet
import ren.natsuyuk1.comet.api.command.CometCommand
import ren.natsuyuk1.comet.api.command.CommandProperty
import ren.natsuyuk1.comet.api.command.PlatformCommandSender
import ren.natsuyuk1.comet.api.message.MessageWrapper
import ren.natsuyuk1.comet.api.platform.LoginPlatform
import ren.natsuyuk1.comet.api.session.SessionManager
import ren.natsuyuk1.comet.api.user.CometUser
import ren.natsuyuk1.comet.util.toMessageWrapper

val START = CommandProperty(
    "start",
    description = "Telegram 平台独占",
    helpText = "这是一条 Telegram 平台独占的命令, 只在特殊情况下需要使用."
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

        val session = SessionManager.getSession(sender, user)

        if (session == null) {
            subject.sendMessage("没有你待处理的会话捏".toMessageWrapper())
            return
        }
    }
}
