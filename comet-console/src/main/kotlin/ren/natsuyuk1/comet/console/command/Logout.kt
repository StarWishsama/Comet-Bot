package ren.natsuyuk1.comet.console.command

import moe.sdl.yac.parameters.arguments.argument
import moe.sdl.yac.parameters.options.default
import moe.sdl.yac.parameters.options.option
import moe.sdl.yac.parameters.types.enum
import moe.sdl.yac.parameters.types.long
import ren.natsuyuk1.comet.api.command.BaseCommand
import ren.natsuyuk1.comet.api.command.CommandProperty
import ren.natsuyuk1.comet.api.command.ConsoleCommandSender
import ren.natsuyuk1.comet.api.message.MessageWrapper
import ren.natsuyuk1.comet.api.platform.LoginPlatform
import ren.natsuyuk1.comet.api.user.CometUser
import ren.natsuyuk1.comet.console.util.logout

internal val LOGOUT = CommandProperty(
    "logout",
    listOf(),
    "注销机器人账号",
    "/logout [id] --platform (登录平台 默认为 MIRAI)\n" +
        "注意: Telegram 平台下, 你的 ID 为 token 中的数字."
)

internal class Logout(
    override val sender: ConsoleCommandSender,
    message: MessageWrapper,
    user: CometUser
) : BaseCommand(sender, message, user, LOGIN) {

    private val id by argument(name = "账户 ID", help = "登录账户的 ID").long()

    private val platform by option(
        "-p",
        "--platform",
        help = "登录 Comet 机器人的平台 (例如 MIRAI, Telegram)"
    ).enum<LoginPlatform>(true).default(LoginPlatform.MIRAI)

    override suspend fun run() {
        when (platform) {
            LoginPlatform.MIRAI -> {
                logout(id, LoginPlatform.MIRAI)
            }

            LoginPlatform.TELEGRAM -> {
                logout(id, LoginPlatform.TELEGRAM)
            }

            else -> {}
        }
    }
}
