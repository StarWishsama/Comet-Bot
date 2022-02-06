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
import io.github.starwishsama.comet.CometVariables.cfg
import io.github.starwishsama.comet.CometVariables.daemonLogger
import io.github.starwishsama.comet.CometVariables.mapper
import io.github.starwishsama.comet.file.DataFiles.allDataFile
import io.github.starwishsama.comet.logger.LoggerInstances
import io.github.starwishsama.comet.managers.ApiManager
import io.github.starwishsama.comet.managers.GroupConfigManager
import io.github.starwishsama.comet.objects.config.CometConfig
import io.github.starwishsama.comet.objects.config.PerGroupConfig
import io.github.starwishsama.comet.service.compatibility.CompatibilityService
import io.github.starwishsama.comet.service.gacha.GachaService
import io.github.starwishsama.comet.utils.*
import net.mamoe.mirai.Bot
import net.mamoe.yamlkt.Yaml.Default
import java.io.File
import java.io.IOException

object DataSetup {
    private var brokenConfig = false

    fun init() {
        daemonLogger.info("正在加载资源文件...")

        allDataFile.forEach {
            try {
                it.check()
            } catch (e: IOException) {
                daemonLogger.warning("在初始化文件 ${it.file.name} 时出现了意外", e)
            }
        }

        try {
            load()
        } catch (e: Exception) {
            brokenConfig = true
            e.message?.let { FileUtil.createErrorReportFile("加载数据文件失败, 正在创建备份...\n", "resource", e, "", it) }
            throw e
        } finally {
            if (brokenConfig) {
                allDataFile.forEach {
                    it.createBackup()
                }
            }
        }
    }

    private fun load() {
        FileUtil.initResourceFile()

        cfg = Default.decodeFromString(CometConfig.serializer(), Config.file.getContext())

        LoggerInstances.instances.forEach {
            it.defaultLevel = cfg.debugMode
        }

        if (CompatibilityService.upgradeUserData(UserConfig.file)) {
            CometVariables.cometUsers.putAll(UserConfig.file.parseAsClass())
        }

        daemonLogger.info("已加载了 ${CometVariables.cometUsers.size} 个用户数据.")

        GachaService.loadGachaInstance()

        ApiManager.loadAllApiConfig()

        daemonLogger.info("数据文件加载完成.")
    }

    fun saveAllResources() {
        Config.save()
        UserConfig.save()
        GroupConfig.save()
        GithubRepoData.save()
        daemonLogger.info("[数据] 自动保存数据完成")
    }

    fun reload() {
        cfg = Default.decodeFromString(CometConfig.serializer(), Config.file.getContext())
        ApiManager.reloadConfig()
    }

    fun initPerGroupSetting(bot: Bot) {
        bot.groups.forEach { group ->
            val loc = File(GroupConfig.file, "${group.id}.json")
            try {
                if (!loc.exists()) {
                    FileUtil.createBlankFile(loc)
                    GroupConfigManager.addConfig(PerGroupConfig(group.id).also { it.init() })
                } else {
                    val cfg: PerGroupConfig = if (loc.getContext().isEmpty()) {
                        daemonLogger.warning("检测到 ${group.id} 的群配置异常, 正在重新生成...")
                        PerGroupConfig(group.id).also {
                            it.init()
                            loc.writeClassToJson(it)
                        }
                    } else {
                        try {
                            loc.parseAsClass(mapper)
                        } catch (e: Exception) {
                            daemonLogger.warning("检测到 ${group.id} 的群配置异常, 正在重新生成...")
                            daemonLogger.debug("加载群配置失败", e)
                            loc.createBackupFile().also { loc.delete() }
                            PerGroupConfig(group.id).also {
                                it.init()
                                loc.writeClassToJson(it)
                            }
                        }
                    }
                    GroupConfigManager.addConfig(cfg)
                }
            } catch (e: RuntimeException) {
                CometVariables.logger.warning("[配置] 在加载 ${group.id} 的分群配置时出现了问题", e)
            }
        }

        CometVariables.logger.info("[配置] 成功加载了 ${GroupConfigManager.getAllConfigs().size} 个群配置")
    }
}