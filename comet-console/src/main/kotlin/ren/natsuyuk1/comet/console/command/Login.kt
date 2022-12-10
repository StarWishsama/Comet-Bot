package ren.natsuyuk1.comet.console.command

import moe.sdl.yac.parameters.arguments.argument
import moe.sdl.yac.parameters.options.default
import moe.sdl.yac.parameters.options.option
import moe.sdl.yac.parameters.types.enum
import moe.sdl.yac.parameters.types.long
import ren.natsuyuk1.comet.api.command.BaseCommand
import ren.natsuyuk1.comet.api.command.CommandProperty
import ren.natsuyuk1.comet.api.command.ConsoleCommandSender
import ren.natsuyuk1.comet.api.database.AccountData
import ren.natsuyuk1.comet.api.message.MessageWrapper
import ren.natsuyuk1.comet.api.message.buildMessageWrapper
import ren.natsuyuk1.comet.api.platform.LoginPlatform
import ren.natsuyuk1.comet.api.platform.MiraiLoginProtocol
import ren.natsuyuk1.comet.api.user.CometUser

internal val LOGIN = CommandProperty(
    "login",
    listOf(),
    "登录机器人账号",
    "/login [ID] [密码]\n" +
        "-p/--platform (登录平台 默认为 QQ)\n" +
        "-P/--protocol 登录协议, 仅在使用 QQ 登录时可用\n" +
        "注意: Telegram 平台下, 你的 ID 为 token 中的**数字**."
)

internal class Login(
    override val sender: ConsoleCommandSender,
    message: MessageWrapper,
    user: CometUser
) : BaseCommand(sender, message, user, LOGIN) {

    private val id by argument(name = "账户 ID", help = "登录账户的 ID").long()

    private val password by argument(name = "账户密码/Token", help = "登录账户的密码/Token")

    private val platform by option(
        "-p",
        "--platform",
        help = "登录 Comet 机器人的平台 (例如 QQ, Telegram)"
    ).enum<LoginPlatform>(true).default(LoginPlatform.MIRAI)

    private val protocol by option(
        "-P",
        "--protocol",
        help = "登录 Comet QQ 侧时使用的协议"
    ).enum<MiraiLoginProtocol>(true).default(MiraiLoginProtocol.ANDROID_PAD)

    override suspend fun run() {
        if (password.isBlank()) {
            sender.sendMessage(buildMessageWrapper { appendText("请输入密码, 例子: login 123456 -p qq -pwd password") })
            return
        }

        when (platform) {
            LoginPlatform.MIRAI -> {
                sender.sendMessage(buildMessageWrapper { appendText("正在尝试登录账号 $id 于 QQ 平台") })

                AccountData.login(id, password, LoginPlatform.MIRAI, protocol)
            }

            LoginPlatform.TELEGRAM -> {
                sender.sendMessage(buildMessageWrapper { appendText("正在尝试登录账号于 Telegram 平台") })

                AccountData.login(id, password, LoginPlatform.TELEGRAM, protocol)
            }

            else -> {}
        }
    }
}
