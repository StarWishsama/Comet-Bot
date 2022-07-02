package ren.natsuyuk1.comet.commands

import ren.natsuyuk1.comet.api.command.CometCommand
import ren.natsuyuk1.comet.api.command.CommandProperty
import ren.natsuyuk1.comet.api.command.PlatformCommandSender
import ren.natsuyuk1.comet.api.user.CometUser
import ren.natsuyuk1.comet.utils.message.MessageWrapper

val PROJECTSEKAI by lazy {
    CommandProperty(
        "projectsekai",
        listOf("pjsk", "啤酒烧烤"),
        "展示 Comet 的帮助菜单",
        "输入 /help 查询命令列表"
    )
}

class ProjectSekaiCommand(
    override val sender: PlatformCommandSender,
    raw: String,
    message: MessageWrapper,
    user: CometUser
) :
    CometCommand(sender, raw, message, user, PROJECTSEKAI) {

    override suspend fun run() {
        TODO("Not yet implemented")
    }
}
