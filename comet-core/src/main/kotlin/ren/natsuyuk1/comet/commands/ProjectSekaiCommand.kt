package ren.natsuyuk1.comet.commands

import ren.natsuyuk1.comet.api.Comet
import ren.natsuyuk1.comet.api.command.CometCommand
import ren.natsuyuk1.comet.api.command.CommandProperty
import ren.natsuyuk1.comet.api.command.PlatformCommandSender
import ren.natsuyuk1.comet.api.user.CometUser
import ren.natsuyuk1.comet.utils.message.MessageWrapper

val PROJECTSEKAI by lazy {
    CommandProperty(
        "projectsekai",
        listOf("pjsk", "啤酒烧烤"),
        "查询 Project Sekai: Colorful Stage 相关信息",
        "TODO"
    )
}

class ProjectSekaiCommand(
    comet: Comet,
    override val sender: PlatformCommandSender,
    message: MessageWrapper,
    user: CometUser
) :
    CometCommand(comet, sender, message, user, PROJECTSEKAI) {

    override suspend fun run() {
        TODO("Not yet implemented")
    }
}
