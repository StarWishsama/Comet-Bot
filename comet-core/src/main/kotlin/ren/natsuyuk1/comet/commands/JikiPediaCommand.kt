package ren.natsuyuk1.comet.commands

import moe.sdl.yac.parameters.arguments.argument
import ren.natsuyuk1.comet.api.Comet
import ren.natsuyuk1.comet.api.command.CometCommand
import ren.natsuyuk1.comet.api.command.CommandProperty
import ren.natsuyuk1.comet.api.command.PlatformCommandSender
import ren.natsuyuk1.comet.api.user.CometUser
import ren.natsuyuk1.comet.network.thirdparty.jikipedia.JikiPediaAPI
import ren.natsuyuk1.comet.utils.message.MessageWrapper

val JIKI = CommandProperty(
    "jiki",
    listOf("小鸡百科", "jikipedia"),
    "小鸡词典 - 网络梗百科全书",
    "/jiki [关键词] 搜索对应词可能的意思" +
        "\n\n数据源来自小鸡词典 \uD83D\uDC25 ",
    executeConsumePoint = 10
)

class JikiPediaCommand(
    comet: Comet,
    override val sender: PlatformCommandSender,
    override val subject: PlatformCommandSender,
    message: MessageWrapper,
    user: CometUser
) : CometCommand(comet, sender, subject, message, user, JIKI) {

    private val keyword by argument(help = "搜索关键词")

    override suspend fun run() {
        subject.sendMessage(JikiPediaAPI.searchByKeyWord(keyword).toMessageWrapper())
    }
}
