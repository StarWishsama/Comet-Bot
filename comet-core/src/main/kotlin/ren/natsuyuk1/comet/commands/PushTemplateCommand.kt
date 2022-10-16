package ren.natsuyuk1.comet.commands

import moe.sdl.yac.core.subcommands
import ren.natsuyuk1.comet.api.Comet
import ren.natsuyuk1.comet.api.command.*
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
    init {
        subcommands()
    }

    override suspend fun run() {
        if (currentContext.invokedSubcommand == null) {
            subject.sendMessage(property.helpText.toMessageWrapper())
        }
    }

    class New(
        override val subject: PlatformCommandSender,
        override val sender: PlatformCommandSender,
        override val user: CometUser
    ) : CometSubCommand(subject, sender, user, NEW) {
        companion object {
            val NEW = SubCommandProperty("new", listOf("新建"), PUSH_TEMPLATE)
        }

        override suspend fun run() {
            TODO("Not yet implemented")
        }
    }

    class Remove(
        override val subject: PlatformCommandSender,
        override val sender: PlatformCommandSender,
        override val user: CometUser
    ) : CometSubCommand(subject, sender, user, REMOVE) {
        companion object {
            val REMOVE = SubCommandProperty("remove", listOf("rm", "删除"), PUSH_TEMPLATE)
        }

        override suspend fun run() {
            TODO("Not yet implemented")
        }
    }

    class Subscribe(
        override val subject: PlatformCommandSender,
        override val sender: PlatformCommandSender,
        override val user: CometUser
    ) : CometSubCommand(subject, sender, user, SUBSCRIBE) {
        companion object {
            val SUBSCRIBE = SubCommandProperty("subscribe", listOf("sub", "订阅"), PUSH_TEMPLATE)
        }

        override suspend fun run() {
            TODO("Not yet implemented")
        }
    }

    class UnSubscribe(
        override val subject: PlatformCommandSender,
        override val sender: PlatformCommandSender,
        override val user: CometUser
    ) : CometSubCommand(subject, sender, user, UNSUBSCRIBE) {
        companion object {
            val UNSUBSCRIBE = SubCommandProperty("unsubscribe", listOf("unsub", "退订"), PUSH_TEMPLATE)
        }

        override suspend fun run() {
            TODO("Not yet implemented")
        }
    }

    class List(
        override val subject: PlatformCommandSender,
        override val sender: PlatformCommandSender,
        override val user: CometUser
    ) : CometSubCommand(subject, sender, user, LIST) {
        companion object {
            val LIST = SubCommandProperty("list", listOf("ls"), PUSH_TEMPLATE)
        }

        override suspend fun run() {
            TODO("Not yet implemented")
        }
    }
}
