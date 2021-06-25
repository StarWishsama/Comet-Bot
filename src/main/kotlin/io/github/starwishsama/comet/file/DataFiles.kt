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
    val allDataFile = listOf(
        UserConfig, Config, ArkNightData, GroupConfig, GithubRepoData
    )
}

object UserConfig : DataFileEntity(File(CometVariables.filePath, "users.json")) {
    override fun init() {
        file.writeClassToJson(CometVariables.cometUsers)
    }

    override fun save() {
        file.writeClassToJson(CometVariables.cometUsers)
    }
}

object Config : DataFileEntity(File(CometVariables.filePath, "config.yml")) {
    override fun init() {
        file.writeString(Default.encodeToString(CometConfig()))
    }

    override fun save() {
        file.writeString(Default.encodeToString(CometVariables.cfg))
    }
}

object ArkNightData : DataFileEntity(File(FileUtil.getResourceFolder(), "arkNights.json")) {
    override fun init() {
        // No need to init
    }

    override fun save() {
        // No need to save
    }
}

object GroupConfig : DataFileEntity(FileUtil.getChildFolder("groups")) {
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

object GithubRepoData : DataFileEntity(File(FileUtil.getResourceFolder(), "repos.yml")) {
    override fun init() {
        file.createNewFile()
    }

    override fun save() {
        file.writeString(Default.encodeToString(GitHubService.repos))
    }
}