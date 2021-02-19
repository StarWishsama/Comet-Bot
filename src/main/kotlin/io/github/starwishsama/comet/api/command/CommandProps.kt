package io.github.starwishsama.comet.api.command

import io.github.starwishsama.comet.BotVariables
import io.github.starwishsama.comet.enums.UserLevel

data class CommandProps(
        val name: String,
        val aliases: List<String> = mutableListOf(),
        val description: String,
        val permission: String,
        val level: UserLevel,
        val consumerType: CommandExecuteConsumerType = CommandExecuteConsumerType.COOLDOWN,
        val consumePoint: Int = BotVariables.cfg.coolDownTime
)