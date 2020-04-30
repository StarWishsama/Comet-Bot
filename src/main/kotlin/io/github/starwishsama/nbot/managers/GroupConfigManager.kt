package io.github.starwishsama.nbot.managers

import io.github.starwishsama.nbot.objects.group.GroupConfig

object GroupConfigManager {
    var configs = mutableMapOf<Long, GroupConfig>()

    fun getConfig(groupId: Long) : GroupConfig {
        return configs.getOrDefault(groupId, GroupConfig(groupId))
    }
}