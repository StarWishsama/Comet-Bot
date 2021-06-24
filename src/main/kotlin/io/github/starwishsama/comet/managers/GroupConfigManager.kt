/*
 * Copyright (c) 2019-2021 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package io.github.starwishsama.comet.managers

import io.github.starwishsama.comet.CometVariables
import io.github.starwishsama.comet.CometVariables.comet
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

        CometVariables.daemonLogger.info("已保存所有群配置")
    }
}