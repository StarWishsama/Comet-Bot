package ren.natsuyuk1.comet.commands

import moe.sdl.yac.core.PrintMessage
import moe.sdl.yac.parameters.options.option
import moe.sdl.yac.parameters.types.double
import org.jetbrains.exposed.sql.transactions.transaction
import ren.natsuyuk1.comet.api.Comet
import ren.natsuyuk1.comet.api.command.CometCommand
import ren.natsuyuk1.comet.api.command.CommandProperty
import ren.natsuyuk1.comet.api.command.PlatformCommandSender
import ren.natsuyuk1.comet.api.message.MessageWrapper
import ren.natsuyuk1.comet.api.user.CometUser
import ren.natsuyuk1.comet.api.user.UserLevel
import ren.natsuyuk1.comet.util.toMessageWrapper
import ren.natsuyuk1.comet.util.yac.user

val COIN = CommandProperty(
    "coin",
    listOf("yb", "硬币"),
    "操作用户硬币",
    permissionLevel = UserLevel.OWNER,
)

class CoinCommand(
    comet: Comet,
    override val sender: PlatformCommandSender,
    override val subject: PlatformCommandSender,
    val message: MessageWrapper,
    val user: CometUser,
) : CometCommand(comet, sender, subject, message, user, COIN) {

    private val target by option("-t", "--target", help = "目标用户").user(message)
    private val deposit by option("-d", "--deposit", help = "增加用户金币").double()
    private val withdraw by option("-w", "--withdraw", help = "减少用户金币").double()

    override suspend fun run() {
        val cometUser = when (target) {
            null -> user
            0L -> throw PrintMessage("获取指定用户失败")
            else -> target?.let { CometUser.getUser(it, sender.platform) } ?: throw PrintMessage("指定的用户并没有注册过")
        }

        when {
            deposit != null -> {
                transaction {
                    cometUser.coin += deposit!!
                }

                subject.sendMessage("操作成功".toMessageWrapper())
            }
            withdraw != null -> {
                transaction {
                    cometUser.coin -= withdraw!!
                }

                subject.sendMessage("操作成功".toMessageWrapper())
            }
        }
    }
}
