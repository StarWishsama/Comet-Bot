package io.github.starwishsama.comet.commands.subcommands.console

import io.github.starwishsama.comet.commands.CommandProps
import io.github.starwishsama.comet.commands.interfaces.ConsoleCommand
import io.github.starwishsama.comet.enums.UserLevel

class TestCommand : ConsoleCommand {
    override suspend fun execute(args: List<String>): String {
        return "测试"
    }

    override fun getProps(): CommandProps = CommandProps("test", null, "", "", UserLevel.CONSOLE)
}