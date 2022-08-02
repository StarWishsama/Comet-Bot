package ren.natsuyuk1.comet.cli.command

import moe.sdl.yac.parameters.arguments.argument
import moe.sdl.yac.parameters.options.default
import moe.sdl.yac.parameters.options.option
import moe.sdl.yac.parameters.types.enum
import moe.sdl.yac.parameters.types.long
import org.jetbrains.exposed.sql.transactions.transaction
import ren.natsuyuk1.comet.api.command.BaseCommand
import ren.natsuyuk1.comet.api.command.CommandProperty
import ren.natsuyuk1.comet.api.command.ConsoleCommandSender
import ren.natsuyuk1.comet.api.platform.LoginPlatform
import ren.natsuyuk1.comet.api.user.CometUser
import ren.natsuyuk1.comet.api.user.UserLevel
import ren.natsuyuk1.comet.utils.message.MessageWrapper
import ren.natsuyuk1.comet.utils.message.buildMessageWrapper


val PROMOTE = CommandProperty(
    "promote",
    listOf(),
    "提升用户权限等级",
    "promote [id] -p/--platform=[平台]"
)

class Promote(
    override val sender: ConsoleCommandSender,
    message: MessageWrapper,
    user: CometUser
) : BaseCommand(sender, message, user, STOP) {

    private val id by argument("QQ/Telegram 平台用户ID").long()
    private val platformName by option("-p", "--platform", help = "平台名称").enum<LoginPlatform>(true)
        .default(LoginPlatform.MIRAI)

    override suspend fun run() {
        val user = CometUser.getUserOrCreate(id, platformName)

        if (user == null) {
            sender.sendMessage(buildMessageWrapper { appendText("找不到此用户") })
            return
        }

        val targetLevel = user.userLevel.ordinal + 1

        transaction {
            if (targetLevel >= UserLevel.values().size) {
                user.userLevel = UserLevel.USER
            } else {
                user.userLevel = UserLevel.values()[targetLevel]
            }

            sender.sendMessage(buildMessageWrapper { appendText("成功设置 $id 的用户等级为 ${user.userLevel}") })
        }
    }
}
