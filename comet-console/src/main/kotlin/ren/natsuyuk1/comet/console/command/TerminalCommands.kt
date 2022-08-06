package ren.natsuyuk1.comet.console.command

import ren.natsuyuk1.comet.api.command.CommandManager
import ren.natsuyuk1.comet.api.command.ConsoleCommandNode

internal val DEFAULT_COMMANDS = listOf(
    ConsoleCommandNode(STOP) { _, sender, _, wrapper, user -> Stop(sender, wrapper, user) },
    ConsoleCommandNode(LOGIN) { _, sender, _, wrapper, user -> Login(sender, wrapper, user) },
    ConsoleCommandNode(PROMOTE) { _, sender, _, wrapper, user -> Promote(sender, wrapper, user) },
    ConsoleCommandNode(LOGOUT) { _, sender, _, wrapper, user -> Logout(sender, wrapper, user) }
)

internal fun registerTerminalCommands() = CommandManager.registerCommands(DEFAULT_COMMANDS)
