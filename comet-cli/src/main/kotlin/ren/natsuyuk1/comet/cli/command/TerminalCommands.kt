package ren.natsuyuk1.comet.cli.command

import ren.natsuyuk1.comet.api.command.CommandManager
import ren.natsuyuk1.comet.api.command.ConsoleCommandNode

val DEFAULT_COMMANDS = listOf(
    ConsoleCommandNode(STOP) { sender, wrapper, user -> Stop(sender, wrapper, user) },
    ConsoleCommandNode(LOGIN) { sender, wrapper, user -> Login(sender, wrapper, user) }
)

fun registerTerminalCommands() = CommandManager.registerCommands(DEFAULT_COMMANDS)
