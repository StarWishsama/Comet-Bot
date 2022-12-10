package ren.natsuyuk1.comet.api.test.command

import ren.natsuyuk1.comet.api.Comet
import ren.natsuyuk1.comet.api.command.CometCommand
import ren.natsuyuk1.comet.api.command.CommandProperty
import ren.natsuyuk1.comet.api.command.PlatformCommandSender
import ren.natsuyuk1.comet.api.message.MessageWrapper
import ren.natsuyuk1.comet.api.message.buildMessageWrapper
import ren.natsuyuk1.comet.api.user.CometUser
import ren.natsuyuk1.comet.api.user.UserLevel

val PERMISSION = CommandProperty(
    "permission",
    listOf("perm"),
    "测试权限",
    "测试权限",
    permissionLevel = UserLevel.OWNER
)

class TestPermissionCommand(
    comet: Comet,
    sender: PlatformCommandSender,
    subject: PlatformCommandSender,
    message: MessageWrapper,
    user: CometUser
) : CometCommand(comet, sender, subject, message, user, PERMISSION) {
    override suspend fun run() {
        subject.sendMessage(
            buildMessageWrapper {
                appendText("You have permission!")
            }
        )
    }
}
