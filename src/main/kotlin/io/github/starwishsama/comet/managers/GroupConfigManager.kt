package io.github.starwishsama.comet.managers

import io.github.starwishsama.comet.BotVariables.bot
import io.github.starwishsama.comet.BotVariables.perGroup
import io.github.starwishsama.comet.objects.group.PerGroupConfig

object GroupConfigManager {
    fun getConfig(groupId: Long): PerGroupConfig? {
        perGroup.forEach {
            if (it.id == groupId) {
                return it
            }
        }

        return null
    }

    fun getConfigOrNew(groupId: Long): PerGroupConfig {
        if (groupId <= 0) throw RuntimeException("群号不允许小于0")
        if (bot.getGroup(groupId) == null) throw RuntimeException("所获取的群不存在")

        val cfg = getConfig(groupId)
        return cfg ?: createNewConfig(groupId)
    }

    fun createNewConfig(groupId: Long): PerGroupConfig {
        return PerGroupConfig(groupId).init()
    }
}