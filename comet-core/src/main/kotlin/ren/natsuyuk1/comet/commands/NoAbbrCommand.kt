package ren.natsuyuk1.comet.commands

import moe.sdl.yac.parameters.arguments.argument
import ren.natsuyuk1.comet.api.Comet
import ren.natsuyuk1.comet.api.command.CometCommand
import ren.natsuyuk1.comet.api.command.CommandProperty
import ren.natsuyuk1.comet.api.command.PlatformCommandSender
import ren.natsuyuk1.comet.api.user.CometUser
import ren.natsuyuk1.comet.commands.service.NoAbbrService.processAbbrSearch
import ren.natsuyuk1.comet.utils.message.MessageWrapper

val NOABBR = CommandProperty(
    "nbnhhsh",
    listOf("noabbr", "能不能好好说话"),
    "能不能好好说话 - 查询缩写含义",
    "/nbnhhsh [缩写] 查询缩写含义"
)

class NoAbbrCommand(
    comet: Comet,
    override val sender: PlatformCommandSender,
    override val subject: PlatformCommandSender,
    message: MessageWrapper,
    user: CometUser
) : CometCommand(comet, sender, subject, message, user, NOABBR) {
    private val keyword by argument("搜索关键词")

    override suspend fun run() {
        subject.processAbbrSearch(keyword)
    }
}
