package ren.natsuyuk1.comet.console.command

import ren.natsuyuk1.comet.api.command.BaseCommand
import ren.natsuyuk1.comet.api.command.CommandProperty
import ren.natsuyuk1.comet.api.command.ConsoleCommandSender
import ren.natsuyuk1.comet.api.message.MessageWrapper
import ren.natsuyuk1.comet.api.user.CometUser
import kotlin.system.exitProcess

internal val STOP = CommandProperty(
    "stop",
    listOf(),
    "关闭 Comet Terminal",
    "/stop 关闭 Comet Terminal"
)

internal class Stop(
    override val sender: ConsoleCommandSender,
    message: MessageWrapper,
    user: CometUser
) : BaseCommand(sender, message, user, STOP) {

    override suspend fun run() {
        exitProcess(0)
    }
}
