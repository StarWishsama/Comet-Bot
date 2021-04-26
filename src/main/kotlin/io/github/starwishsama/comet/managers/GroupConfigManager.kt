package io.github.starwishsama.comet.managers

import io.github.starwishsama.comet.BotVariables
import io.github.starwishsama.comet.BotVariables.comet
import io.github.starwishsama.comet.objects.config.PerGroupConfig
import io.github.starwishsama.comet.utils.FileUtil
import io.github.starwishsama.comet.utils.writeClassToJson
import java.io.File

object GroupConfigManager {
    private val groupConfigs: MutableSet<PerGroupConfig> = HashSet()

    fun getConfig(groupId: Long): PerGroupConfig? {
        if (groupConfigs.isEmpty()) return null

        groupConfigs.forEach {
            if (groupId == it.id) {
                return it
            }
        }

        return null
    }

    fun getConfigOrNew(groupId: Long): PerGroupConfig {
        require(groupId > 0) { "群号不允许小于0" }
        requireNotNull(comet.getBot().getGroup(groupId)) { "所获取的群不存在" }

        val cfg = getConfig(groupId)
        return cfg ?: createNewConfig(groupId)
    }

    fun createNewConfig(groupId: Long, instantInit: Boolean = true): PerGroupConfig {
        return PerGroupConfig(groupId).also { if (instantInit) it.init() }
    }

    fun expireConfig(groupId: Long) {
        groupConfigs.removeIf { groupId == it.id }
    }

    fun addConfig(config: PerGroupConfig) {
        groupConfigs.add(config)
    }

    fun removeConfig(config: PerGroupConfig) {
        groupConfigs.remove(config)
    }

    fun getAllConfigs(): Set<PerGroupConfig> = groupConfigs

    fun saveAll() {
        if (!FileUtil.getChildFolder("groups").exists()) {
            FileUtil.getChildFolder("groups").mkdirs()
        }

        getAllConfigs().forEach {
            val loc = File(FileUtil.getChildFolder("groups"), "${it.id}.json")
            if (!loc.exists()) {
                loc.createNewFile()
            }
            loc.writeClassToJson(it)
        }

        BotVariables.daemonLogger.info("已保存所有群配置")
    }
}