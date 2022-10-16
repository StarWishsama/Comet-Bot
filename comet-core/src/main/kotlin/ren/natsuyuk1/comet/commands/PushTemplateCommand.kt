package ren.natsuyuk1.comet.commands

import ren.natsuyuk1.comet.api.Comet
import ren.natsuyuk1.comet.api.command.CometCommand
import ren.natsuyuk1.comet.api.command.CommandProperty
import ren.natsuyuk1.comet.api.command.PlatformCommandSender
import ren.natsuyuk1.comet.api.message.MessageWrapper
import ren.natsuyuk1.comet.api.user.CometUser
import ren.natsuyuk1.comet.api.user.UserLevel
import ren.natsuyuk1.comet.util.groupAdminChecker
import ren.natsuyuk1.comet.util.toMessageWrapper

val PUSH_TEMPLATE = CommandProperty(
    "pushtemplate",
    listOf("推送模板", "ptemplate", "ptl"),
    "管理推送模板并订阅",
    """
    /ptl new [模板名] 新建一个模板
    /ptl rm/remove [模板名] 删除一个模板 
    /ptl sub/subscribe [模板名]
    /ptl unsub/unsubscribe [模板名]
    /ptl list/ls 列出所有模板
    """.trimIndent(),
    permissionLevel = UserLevel.ADMIN,
    extraPermissionChecker = groupAdminChecker
)

class PushTemplateCommand(
    comet: Comet,
    override val sender: PlatformCommandSender,
    override val subject: PlatformCommandSender,
    val message: MessageWrapper,
    user: CometUser
) : CometCommand(comet, sender, subject, message, user, PUSH_TEMPLATE) {
    override suspend fun run() {
        if (currentContext.invokedSubcommand == null) {
            subject.sendMessage(property.helpText.toMessageWrapper())
        }
    }
}
