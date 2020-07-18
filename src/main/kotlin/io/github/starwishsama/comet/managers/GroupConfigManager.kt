package io.github.starwishsama.comet.managers

import io.github.starwishsama.comet.objects.group.PerGroupConfig

object GroupConfigManager {
    var configs = mutableMapOf<Long, PerGroupConfig>()

    fun getConfig(groupId: Long): PerGroupConfig {
        return configs.getOrDefault(groupId, PerGroupConfig(groupId))
    }
}