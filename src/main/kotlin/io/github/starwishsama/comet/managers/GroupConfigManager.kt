package io.github.starwishsama.comet.managers

import io.github.starwishsama.comet.objects.group.GroupConfig

object GroupConfigManager {
    var configs = mutableMapOf<Long, GroupConfig>()

    fun getConfig(groupId: Long) : GroupConfig {
        return configs.getOrDefault(groupId, GroupConfig(groupId))
    }
}