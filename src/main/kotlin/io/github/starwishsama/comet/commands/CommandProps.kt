package io.github.starwishsama.comet.commands

import io.github.starwishsama.comet.enums.UserLevel

data class CommandProps(
    var name: String,
    var aliases: List<String>?,
    var description: String,
    var permission: String,
    var level: UserLevel
)