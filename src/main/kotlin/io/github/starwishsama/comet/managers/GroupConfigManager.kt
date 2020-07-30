package io.github.starwishsama.comet.managers

import io.github.starwishsama.comet.BotVariables
import io.github.starwishsama.comet.objects.group.PerGroupConfig

object GroupConfigManager {
    fun getConfig(groupId: Long): PerGroupConfig? {
        BotVariables.perGroup.forEach {
            if (it.groupId == groupId) {
                return it
            }
        }
        return null
    }

    fun getConfigSafely(groupId: Long): PerGroupConfig {
        val cfg = getConfig(groupId)
        return cfg ?: PerGroupConfig(groupId).init()
    }
}