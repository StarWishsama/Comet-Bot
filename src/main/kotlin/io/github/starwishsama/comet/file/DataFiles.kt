/*
 * Copyright (c) 2019-2021 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package io.github.starwishsama.comet.file

import io.github.starwishsama.comet.CometVariables
import io.github.starwishsama.comet.managers.GroupConfigManager
import io.github.starwishsama.comet.objects.config.CometConfig
import io.github.starwishsama.comet.objects.config.DataFileEntity
import io.github.starwishsama.comet.service.command.GitHubService
import io.github.starwishsama.comet.utils.FileUtil
import io.github.starwishsama.comet.utils.writeClassToJson
import io.github.starwishsama.comet.utils.writeString
import net.mamoe.yamlkt.Yaml.Default
import java.io.File

object DataFiles {
    val userCfg: DataFileEntity =
        object : DataFileEntity(File(CometVariables.filePath, "users.json"), FilePriority.HIGH) {
            override fun init() {
                file.writeClassToJson(CometVariables.cometUsers)
            }

            override fun save() {
                if (CometVariables.cometUsers.isNotEmpty()) {
                    file.writeClassToJson(CometVariables.cometUsers)
                }
            }
        }

    val cfgFile: DataFileEntity =
        object : DataFileEntity(File(CometVariables.filePath, "config.yml"), FilePriority.HIGH) {
            override fun init() {
                file.writeString(Default.encodeToString(CometConfig()), isAppend = false)
            }

            override fun save() {
                if (!CometVariables.cfg.isEmpty()) {
                    file.writeString(
                        Default.encodeToString(CometConfig.serializer(), CometVariables.cfg),
                        isAppend = false
                    )
                }
            }
        }

    val arkNightData: DataFileEntity =
        object : DataFileEntity(File(FileUtil.getResourceFolder(), "arkNights.json"), FilePriority.NORMAL) {
            override fun init() {
                // No need to init
            }

            override fun save() {
                // No need to save
            }
        }

    val perGroupFolder: DataFileEntity =
        object : DataFileEntity(FileUtil.getChildFolder("groups"), FilePriority.NORMAL) {
            override fun init() {
                file.mkdirs()
            }

            override fun save() {
                if (!file.exists()) {
                    file.mkdirs()
                }

                GroupConfigManager.getAllConfigs().forEach {
                    val loc = File(file, "${it.id}.json")
                    if (!loc.exists()) {
                        loc.createNewFile()
                    }
                    loc.writeClassToJson(it)
                }

                CometVariables.daemonLogger.info("已保存所有群配置")
            }
        }

    val githubRepoData: DataFileEntity =
        object : DataFileEntity(File(FileUtil.getResourceFolder(), "repos.yml"), FilePriority.NORMAL) {
            override fun init() {
                file.createNewFile()
            }

            override fun save() {
                file.writeString(Default.encodeToString(GitHubService.repos))
            }
        }

    val allDataFile = listOf(
        userCfg, cfgFile, arkNightData, perGroupFolder
    )
}