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

import io.github.starwishsama.comet.BotVariables
import io.github.starwishsama.comet.BotVariables.cfg
import io.github.starwishsama.comet.BotVariables.daemonLogger
import io.github.starwishsama.comet.BotVariables.mapper
import io.github.starwishsama.comet.i18n.LocalizationManager
import io.github.starwishsama.comet.logger.LoggerInstances
import io.github.starwishsama.comet.managers.ApiManager
import io.github.starwishsama.comet.managers.GroupConfigManager
import io.github.starwishsama.comet.objects.config.CometConfig
import io.github.starwishsama.comet.objects.config.DataFile
import io.github.starwishsama.comet.objects.config.PerGroupConfig
import io.github.starwishsama.comet.service.compatibility.CompatibilityService
import io.github.starwishsama.comet.service.gacha.GachaService
import io.github.starwishsama.comet.utils.*
import net.mamoe.mirai.Bot
import net.mamoe.yamlkt.Yaml.Default
import java.io.File
import java.io.IOException

object DataSetup {
    private val userCfg: DataFile = DataFile(File(BotVariables.filePath, "users.json"), DataFile.FilePriority.HIGH) {
        it.writeClassToJson(BotVariables.users)
    }
    private val cfgFile: DataFile = DataFile(File(BotVariables.filePath, "config.yml"), DataFile.FilePriority.HIGH) {
        it.writeString(Default.encodeToString(CometConfig()), isAppend = false)
    }
    val pcrData: DataFile = DataFile(File(FileUtil.getResourceFolder(), "pcr.json"), DataFile.FilePriority.NORMAL)
    private val arkNightData: DataFile =
        DataFile(File(FileUtil.getResourceFolder(), "arkNights.json"), DataFile.FilePriority.NORMAL)
    private val perGroupFolder: DataFile = DataFile(FileUtil.getChildFolder("groups"), DataFile.FilePriority.NORMAL) {
        it.mkdirs()
    }
    private val dataFiles = listOf(
        userCfg, cfgFile, pcrData, arkNightData, perGroupFolder
    )
    private var brokenConfig = false

    fun init() {
        dataFiles.forEach {
            try {
                if (it.file.exists()) return@forEach

                if (it.priority >= DataFile.FilePriority.NORMAL) {
                    it.initAction(it.file)
                }
            } catch (e: IOException) {
                daemonLogger.warning("在初始化文件 ${it.file.name} 时出现了意外", e)
            }
        }

        try {
            load()
        } catch (e: Exception) {
            brokenConfig = true
            e.message?.let { FileUtil.createErrorReportFile("加载配置文件失败, 部分配置文件将会立即创建备份\n", "resource", e, "", it) }
            throw e
        } finally {
            if (brokenConfig) {
                cfgFile.file.createBackupFile()
                userCfg.file.createBackupFile()
            }
        }
    }

    private fun saveCfg() {
        try {
            cfgFile.file.writeString(Default.encodeToString(CometConfig.serializer(), cfg), isAppend = false)
            userCfg.file.writeClassToJson(BotVariables.users)
        } catch (e: Exception) {
            daemonLogger.warning("[配置] 在保存配置文件时发生了问题", e)
        }
    }

    private fun load() {
        cfg = Default.decodeFromString(CometConfig.serializer(), cfgFile.file.getContext())

        LoggerInstances.instances.forEach {
            it.debugMode = cfg.debugMode
        }

        if (CompatibilityService.checkUserData(userCfg.file)) {
            BotVariables.users.putAll(userCfg.file.parseAsClass())
        }

        daemonLogger.info("已加载了 ${BotVariables.users.size} 个用户数据.")

        FileUtil.initResourceFile()

        BotVariables.localizationManager = LocalizationManager()

        GachaService.loadGachaData(arkNightData.file, pcrData.file)

        ApiManager.loadAllApiConfig()
    }

    fun saveAllResources() {
        daemonLogger.info("[数据] 自动保存数据完成")
        saveCfg()
        GroupConfigManager.saveAll()
    }

    fun reload() {
        cfg = Default.decodeFromString(CometConfig.serializer(), cfgFile.file.getContext())
        ApiManager.reloadConfig()
    }

    fun initPerGroupSetting(bot: Bot) {
        bot.groups.forEach { group ->
            val loc = File(perGroupFolder.file, "${group.id}.json")
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
                BotVariables.logger.warning("[配置] 在加载 ${group.id} 的分群配置时出现了问题", e)
            }
        }

        BotVariables.logger.info("[配置] 成功加载了 ${GroupConfigManager.getAllConfigs().size} 个群配置")
    }
}