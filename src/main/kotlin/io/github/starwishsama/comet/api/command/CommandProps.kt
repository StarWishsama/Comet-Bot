package io.github.starwishsama.comet.api.command

import io.github.starwishsama.comet.enums.UserLevel

data class CommandProps(
        val name: String,
        val aliases: List<String> = mutableListOf(),
        val description: String,
        val permission: String,
        val level: UserLevel
)