package io.github.starwishsama.comet.commands.console

import io.github.starwishsama.comet.api.command.CommandProps
import io.github.starwishsama.comet.api.command.interfaces.ConsoleCommand

class InfoCommand: ConsoleCommand {
    override suspend fun execute(args: List<String>): String {
        TODO("Not yet implemented")
    }

    override fun getProps(): CommandProps {
        TODO("Not yet implemented")
    }

    override fun getHelp(): String {
        TODO("Not yet implemented")
    }
}