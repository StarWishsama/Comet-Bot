package io.github.starwishsama.comet.file

import cn.hutool.core.io.file.FileReader
import com.github.salomonbrys.kotson.forEach
import com.github.salomonbrys.kotson.fromJson
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import com.google.gson.stream.JsonReader
import io.github.starwishsama.comet.BotVariables
import io.github.starwishsama.comet.BotVariables.arkNight
import io.github.starwishsama.comet.BotVariables.cfg
import io.github.starwishsama.comet.BotVariables.daemonLogger
import io.github.starwishsama.comet.BotVariables.gson
import io.github.starwishsama.comet.BotVariables.hiddenOperators
import io.github.starwishsama.comet.BotVariables.nullableGson
import io.github.starwishsama.comet.managers.GachaManager
import io.github.starwishsama.comet.managers.GroupConfigManager
import io.github.starwishsama.comet.objects.BotLocalization
import io.github.starwishsama.comet.objects.BotUser
import io.github.starwishsama.comet.objects.config.CometConfig
import io.github.starwishsama.comet.objects.config.DataFile
import io.github.starwishsama.comet.objects.config.PerGroupConfig
import io.github.starwishsama.comet.utils.*
import io.github.starwishsama.comet.utils.RuntimeUtil.getOsName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import net.mamoe.mirai.Bot
import net.mamoe.yamlkt.Yaml.Default
import java.io.File
import java.io.IOException
import java.io.StringReader

object DataSetup {
    private val userCfg: DataFile = DataFile(File(BotVariables.filePath, "users.json"), DataFile.FilePriority.HIGH) {
        it.writeClassToJson(BotVariables.users)
    }
    private val shopItemCfg: DataFile = DataFile(File(BotVariables.filePath, "items.json"), DataFile.FilePriority.NORMAL) {
        it.writeClassToJson(BotVariables.shop)
    }
    private val cfgFile: DataFile = DataFile(File(BotVariables.filePath, "config.yml"), DataFile.FilePriority.HIGH) {
        it.writeString(Default.encodeToString(CometConfig()), isAppend = false)
    }
    @Suppress("DEPRECATION")
    private val langCfg: DataFile = DataFile(File(FileUtil.getResourceFolder(), "lang.json"), DataFile.FilePriority.NORMAL) {
        val default = arrayOf(BotLocalization("msg.bot-prefix", "Bot > "),
            BotLocalization("msg.no-permission", "你没有权限"),
            BotLocalization("msg.bind-success", "绑定账号 %s 成功!"),
            BotLocalization("checkin.first-time", "你还没有签到过, 先用 /qd 签到一次吧~")
        )
        for (text in default) {
            BotVariables.localMessage.plusAssign(text)
        }
        it.writeClassToJson(BotVariables.localMessage)
    }
    private val pcrData: DataFile = DataFile(File(FileUtil.getResourceFolder(), "pcr.json"), DataFile.FilePriority.NORMAL)
    private val arkNightData: DataFile = DataFile(File(FileUtil.getResourceFolder(), "arkNights.json"), DataFile.FilePriority.NORMAL)
    private val perGroupFolder: DataFile = DataFile(FileUtil.getChildFolder("groups"), DataFile.FilePriority.NORMAL) {
        it.mkdirs()
    }
    private val dataFiles = listOf(
        userCfg, shopItemCfg, cfgFile, langCfg, pcrData, arkNightData, perGroupFolder
    )
    private var brokenConfig = false

    fun init() {
        dataFiles.forEach {
            try {
                if (it.file.exists()) return@forEach

                if (it.priority >= DataFile.FilePriority.NORMAL) {
                    it.initAction(it.file)
                }
            } catch (e: RuntimeException) {
                daemonLogger.warning("在初始化文件时出现了意外", e)
            }
        }

        try {
            load()
        } catch (e: RuntimeException) {
            brokenConfig = true
            e.message?.let { FileUtil.createErrorReportFile("加载配置文件失败, 部分配置文件将会立即创建备份\n", "resource", e, "", it) }
            daemonLogger.debug("加载配置文件失败", e)
        } finally {
            if (brokenConfig) {
                cfgFile.file.createBackupFile()
                userCfg.file.createBackupFile()
                shopItemCfg.file.createBackupFile()
            }
        }
    }

    private fun saveCfg() {
        try {
            cfgFile.file.writeString(Default.encodeToString(CometConfig.serializer(), cfg), isAppend = false)
            userCfg.file.writeClassToJson(BotVariables.users)
            shopItemCfg.file.writeClassToJson(BotVariables.shop)
        } catch (e: Exception) {
            daemonLogger.warning("[配置] 在保存配置文件时发生了问题", e)
        }
    }

    private fun load() {
        cfg = Default.decodeFromString(CometConfig.serializer(), cfgFile.file.getContext())

        gson.fromJson<List<BotUser>>(userCfg.file.getContext()).forEach {
            BotUser.addUser(it)
        }


        BotVariables.shop.addAll(nullableGson.fromJson(shopItemCfg.file.getContext()))

        loadLang()

        FileUtil.initResourceFile()

        if (pcrData.exists()) {
            BotVariables.pcr.addAll(nullableGson.fromJson(pcrData.file.getContext()))
            daemonLogger.info("成功载入公主连结游戏数据, 共 ${BotVariables.pcr.size} 个角色")
        } else {
            daemonLogger.info("未检测到公主连结游戏数据, 对应游戏抽卡模拟器将无法使用")
            GachaManager.pcrUsable = false
        }

        try {
            GachaUtil.arkNightDataCheck(arkNightData.file)
        } catch (e: IOException) {
            daemonLogger.warning("解析明日方舟游戏数据失败, ${e.message}\n注意: 数据来源于 Github, 国内用户无法下载请自行下载替换\n替换位置: ./res/arkNights.json\n链接: ${GachaUtil.arkNightData}")
        }

        if (arkNightData.exists()) {
            @Suppress("UNCHECKED_CAST")
            hiddenOperators.addAll(Default.decodeMapFromString(
                File(
                    FileUtil.getResourceFolder(),
                    "hidden_operators.yml"
                ).getContext()
            )["hiddenOperators"] as MutableList<String>)

            JsonParser.parseString(arkNightData.file.getContext()).asJsonObject.forEach { _, e ->
                arkNight.add(nullableGson.fromJson(e))
            }

            daemonLogger.info("成功载入明日方舟游戏数据, 共 (${arkNight.size - hiddenOperators.size}/${arkNight.size}) 个")
            if (cfg.arkDrawUseImage) {
                if (System.getProperty("java.awt.headless") != "true" && getOsName().toLowerCase().contains("linux")) {
                    daemonLogger.info("检测到 Linux 系统, 正在启用无头模式")
                    System.setProperty("java.awt.headless", "true")
                }

                TaskUtil.runAsync {
                    runBlocking {
                        withContext(Dispatchers.IO) {
                            GachaUtil.downloadArkNightsFile()
                        }
                    }
                }
            }
        } else {
            daemonLogger.info("未检测到明日方舟游戏数据, 抽卡模拟器将无法使用")
            GachaManager.arkNightUsable = false
        }

        daemonLogger.info("[配置] 成功载入配置文件")
    }

    private fun loadLang() {
        if (BotVariables.localMessage.isEmpty()) {
            val lang: JsonElement =
                JsonParser.parseReader(JsonReader(StringReader(langCfg.file.getContext())).also { it.isLenient = true })
            if (lang.isJsonArray) {
                BotVariables.localMessage = nullableGson.fromJson(FileReader.create(langCfg.file).readString())
                daemonLogger.info("[配置] 成功载入多语言文件")
            } else {
                daemonLogger.warning("[配置] 在读取多语言文件时发生异常")
            }
        }
    }


    fun saveAllResources() {
        daemonLogger.info("[数据] 自动保存数据完成")
        saveCfg()
        langCfg.file.writeClassToJson(BotVariables.localMessage)
        savePerGroupSetting()
    }

    fun reload() {
        // 仅重载配置文件
        cfg = Default.decodeFromString(CometConfig.serializer(), cfgFile.file.getContext())
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
                            if (ConfigConverter.convertOldGroupConfig(loc)) {
                                return@forEach
                            } else {
                                loc.parseAsClass(PerGroupConfig::class.java, gson)
                            }
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
            }
            catch (e: RuntimeException) {
                BotVariables.logger.warning("[配置] 在加载 ${group.id} 的分群配置时出现了问题", e)
            }
        }

        BotVariables.logger.info("[配置] 成功加载了 ${GroupConfigManager.getAllConfigs().size} 个群配置")
    }

    private fun savePerGroupSetting() {
        if (!perGroupFolder.exists()) return

        GroupConfigManager.getAllConfigs().forEach {
            val loc = File(perGroupFolder.file, "${it.id}.json")
            if (!loc.exists()) loc.createNewFile()
            loc.writeClassToJson(it, gson)
        }

        daemonLogger.info("已保存所有群配置")
    }
}