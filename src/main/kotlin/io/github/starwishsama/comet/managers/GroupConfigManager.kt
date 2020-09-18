package io.github.starwishsama.comet.managers

import io.github.starwishsama.comet.BotVariables.perGroup
import io.github.starwishsama.comet.objects.group.PerGroupConfig

object GroupConfigManager {
    fun getConfig(groupId: Long): PerGroupConfig? {
        val result = perGroup.parallelStream().filter { it.id == groupId }.findFirst()
        return if (result.isPresent) result.get() else null
    }

    fun getConfigSafely(groupId: Long): PerGroupConfig {
        val cfg = getConfig(groupId)
        return cfg ?: PerGroupConfig(groupId).init()
    }
}