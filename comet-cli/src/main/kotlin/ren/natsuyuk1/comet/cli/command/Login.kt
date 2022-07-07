package ren.natsuyuk1.comet.cli.command

import moe.sdl.yac.parameters.arguments.argument
import moe.sdl.yac.parameters.options.option
import moe.sdl.yac.parameters.types.enum
import moe.sdl.yac.parameters.types.long
import ren.natsuyuk1.comet.api.command.CometCommand
import ren.natsuyuk1.comet.api.command.CommandProperty
import ren.natsuyuk1.comet.api.command.ConsoleCommandSender
import ren.natsuyuk1.comet.api.config.CometConfig
import ren.natsuyuk1.comet.api.user.CometUser
import ren.natsuyuk1.comet.cli.CometTerminal
import ren.natsuyuk1.comet.cli.storage.AccountData
import ren.natsuyuk1.comet.cli.storage.LoginPlatform
import ren.natsuyuk1.comet.mirai.MiraiComet
import ren.natsuyuk1.comet.mirai.config.MiraiConfig
import ren.natsuyuk1.comet.mirai.config.findMiraiConfigByID
import ren.natsuyuk1.comet.utils.message.MessageWrapper
import ren.natsuyuk1.comet.utils.message.buildMessageWrapper

val LOGIN = CommandProperty(
    "login",
    listOf(),
    "关闭 Comet Terminal",
    "/stop 关闭 Comet Terminal"
)

class Login(
    override val sender: ConsoleCommandSender,
    message: MessageWrapper,
    user: CometUser
) : CometCommand(sender, message, user, LOGIN) {

    private val platform by argument(
        name = "登录平台",
        help = "登录 Comet 机器人的平台 (例如 QQ, Telegram)"
    ).enum<LoginPlatform>(true)

    private val id by argument(name = "账户 ID", help = "登录账户的 ID").long()

    private val password by option("-p", "--password", help = "登录账户的密码")

    override suspend fun run() {
        if (password == null) {
            sender.sendMessage(buildMessageWrapper { appendText("请输入密码, 例子: login qq 123456 -p=password") })
            return
        }

        when (platform) {
            LoginPlatform.QQ -> handleQQLogin(id, password!!)
            LoginPlatform.TELEGRAM -> handleTelegramLogin(id, password!!)
        }
    }

    private suspend fun handleQQLogin(id: Long, password: String) {
        sender.sendMessage(buildMessageWrapper { appendText("正在尝试登录账号 $id 于 QQ 平台") })
        val miraiConfig = findMiraiConfigByID(id) ?: MiraiConfig(id, password).also { it.init() }
        val miraiComet = MiraiComet(CometConfig, miraiConfig)
        CometTerminal.instance.push(miraiComet)
        miraiComet.login()
        AccountData.registerAccount(id, password, LoginPlatform.QQ)
    }

    private fun handleTelegramLogin(id: Long, password: String) {
        sender.sendMessage(buildMessageWrapper { appendText("正在尝试登录账号 $id 于 Telegram 平台") })
        AccountData.registerAccount(id, password, LoginPlatform.TELEGRAM)
    }
}
