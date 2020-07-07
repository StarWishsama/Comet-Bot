package io.github.starwishsama.nbot.commands.subcommands.console

import io.github.starwishsama.nbot.commands.CommandProps
import io.github.starwishsama.nbot.commands.interfaces.ConsoleCommand
import io.github.starwishsama.nbot.enums.UserLevel
import kotlin.system.exitProcess

class StopCommand : ConsoleCommand {
    override suspend fun execute(args: List<String>): String {
        exitProcess(0)
    }

    override fun getProps(): CommandProps = CommandProps("stop", null, "", "", UserLevel.CONSOLE)
}