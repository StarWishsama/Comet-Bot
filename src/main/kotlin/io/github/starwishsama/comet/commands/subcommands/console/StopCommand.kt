package io.github.starwishsama.comet.commands.subcommands.console

import io.github.starwishsama.comet.commands.CommandProps
import io.github.starwishsama.comet.commands.interfaces.ConsoleCommand
import io.github.starwishsama.comet.enums.UserLevel
import kotlin.system.exitProcess

class StopCommand : ConsoleCommand {
    override suspend fun execute(args: List<String>): String {
        exitProcess(0)
    }

    override fun getProps(): CommandProps = CommandProps("stop", null, "", "", UserLevel.CONSOLE)
}