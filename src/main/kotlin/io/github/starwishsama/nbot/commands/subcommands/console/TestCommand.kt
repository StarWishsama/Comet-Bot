package io.github.starwishsama.nbot.commands.subcommands.console

import io.github.starwishsama.nbot.commands.CommandProps
import io.github.starwishsama.nbot.commands.interfaces.ConsoleCommand
import io.github.starwishsama.nbot.enums.UserLevel

class TestCommand : ConsoleCommand {
    override suspend fun execute(args: List<String>): String {
        return "测试"
    }

    override fun getProps(): CommandProps = CommandProps("test", null, "", "", UserLevel.CONSOLE)
}