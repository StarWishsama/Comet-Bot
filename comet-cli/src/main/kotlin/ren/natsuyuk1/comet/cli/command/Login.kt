package ren.natsuyuk1.comet.cli.command

import moe.sdl.yac.parameters.arguments.argument
import moe.sdl.yac.parameters.options.default
import moe.sdl.yac.parameters.options.option
import moe.sdl.yac.parameters.types.enum
import moe.sdl.yac.parameters.types.long
import ren.natsuyuk1.comet.api.command.BaseCommand
import ren.natsuyuk1.comet.api.command.CommandProperty
import ren.natsuyuk1.comet.api.command.ConsoleCommandSender
import ren.natsuyuk1.comet.api.platform.LoginPlatform
import ren.natsuyuk1.comet.api.user.CometUser
import ren.natsuyuk1.comet.cli.util.login
import ren.natsuyuk1.comet.utils.message.MessageWrapper
import ren.natsuyuk1.comet.utils.message.buildMessageWrapper

internal val LOGIN = CommandProperty(
    "login",
    listOf(),
    "登录机器人账号",
    "/login [id] --password [密码] --platform (登录平台 默认为 QQ)\n" +
        "注意: Telegram 平台下, 你的 ID 为 token 中的数字."
)

internal class Login(
    override val sender: ConsoleCommandSender,
    message: MessageWrapper,
    user: CometUser
) : BaseCommand(sender, message, user, LOGIN) {

    private val id by argument(name = "账户 ID", help = "登录账户的 ID").long()

    private val password by option("-pwd", "--password", help = "登录账户的密码")

    private val platform by option(
        "-p", "--platform",
        help = "登录 Comet 机器人的平台 (例如 QQ, Telegram)"
    ).enum<LoginPlatform>(true).default(LoginPlatform.MIRAI)

    override suspend fun run() {
        if (password == null) {
            sender.sendMessage(buildMessageWrapper { appendText("请输入密码, 例子: login 123456 -p qq -pwd password") })
            return
        }

        when (platform) {
            LoginPlatform.MIRAI -> {
                sender.sendMessage(buildMessageWrapper { appendText("正在尝试登录账号 $id 于 QQ 平台") })

                login(id, password!!, LoginPlatform.MIRAI)
            }

            LoginPlatform.TELEGRAM -> {
                sender.sendMessage(buildMessageWrapper { appendText("正在尝试登录账号于 Telegram 平台") })

                login(id, password!!, LoginPlatform.TELEGRAM)
            }

            else -> {}
        }
    }
}
