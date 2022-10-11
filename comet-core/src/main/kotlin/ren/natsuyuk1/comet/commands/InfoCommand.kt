package ren.natsuyuk1.comet.commands

import kotlinx.datetime.toJavaInstant
import ren.natsuyuk1.comet.api.Comet
import ren.natsuyuk1.comet.api.command.CometCommand
import ren.natsuyuk1.comet.api.command.CommandProperty
import ren.natsuyuk1.comet.api.command.PlatformCommandSender
import ren.natsuyuk1.comet.api.message.AtElement
import ren.natsuyuk1.comet.api.message.MessageWrapper
import ren.natsuyuk1.comet.api.message.buildMessageWrapper
import ren.natsuyuk1.comet.api.user.CometUser
import ren.natsuyuk1.comet.utils.math.NumberUtil.getBetterNumber
import ren.natsuyuk1.comet.utils.time.yyMMddWithTimePattern

val INFO = CommandProperty(
    "info",
    listOf("cx", "查询"),
    "查询账户信息",
    "/info 查询账户信息"
)

class InfoCommand(
    comet: Comet,
    override val sender: PlatformCommandSender,
    override val subject: PlatformCommandSender,
    val message: MessageWrapper,
    private val user: CometUser
) : CometCommand(comet, sender, subject, message, user, INFO) {
    override suspend fun run() {
        subject.sendMessage(
            buildMessageWrapper {
                appendElement(AtElement(sender.id, sender.name))
                appendLine()
                appendText("等级 ${user.level} | 硬币 ${user.coin.getBetterNumber()}", true)
                appendText("上次签到于 ${yyMMddWithTimePattern.format(user.checkInDate.toJavaInstant())}")
            }
        )
    }
}
