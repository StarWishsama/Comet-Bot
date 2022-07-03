package ren.natsuyuk1.comet.cli.command

import ren.natsuyuk1.comet.api.command.CometCommand
import ren.natsuyuk1.comet.api.command.CommandProperty
import ren.natsuyuk1.comet.api.command.ConsoleCommandSender
import ren.natsuyuk1.comet.api.user.CometUser
import ren.natsuyuk1.comet.utils.message.MessageWrapper
import kotlin.system.exitProcess

val STOP = CommandProperty(
    "stop",
    listOf(),
    "关闭 Comet Terminal",
    "/stop 关闭 Comet Terminal"
)

class Stop(
    override val sender: ConsoleCommandSender,
    message: MessageWrapper,
    user: CometUser
) : CometCommand(sender, message, user, STOP) {

    override suspend fun run() {
        exitProcess(0)
    }
}
