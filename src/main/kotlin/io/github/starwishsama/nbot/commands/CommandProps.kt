package io.github.starwishsama.nbot.commands

import io.github.starwishsama.nbot.enums.UserLevel

data class CommandProps(var name: String, var aliases: List<String>?, var permission: String, var level: UserLevel)