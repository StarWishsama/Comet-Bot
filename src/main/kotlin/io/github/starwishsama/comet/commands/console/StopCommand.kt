package io.github.starwishsama.comet.commands.console

import io.github.starwishsama.comet.api.annotations.CometCommand
import io.github.starwishsama.comet.api.command.CommandProps
import io.github.starwishsama.comet.api.command.interfaces.ConsoleCommand
import io.github.starwishsama.comet.enums.UserLevel
import kotlin.system.exitProcess

@CometCommand
class StopCommand : ConsoleCommand {
    override suspend fun execute(args: List<String>): String {
        exitProcess(0)
    }

    override fun getProps(): CommandProps = CommandProps("stop", mutableListOf(), "", "", UserLevel.CONSOLE)
}