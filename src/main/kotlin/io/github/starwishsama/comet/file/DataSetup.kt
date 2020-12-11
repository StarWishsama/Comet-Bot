package io.github.starwishsama.comet.file

import cn.hutool.core.io.file.FileReader
import com.github.salomonbrys.kotson.fromJson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import io.github.starwishsama.comet.BotVariables
import io.github.starwishsama.comet.BotVariables.arkNight
import io.github.starwishsama.comet.BotVariables.cfg
import io.github.starwishsama.comet.BotVariables.daemonLogger
import io.github.starwishsama.comet.BotVariables.gson
import io.github.starwishsama.comet.objects.BotLocalization
import io.github.starwishsama.comet.objects.BotUser
import io.github.starwishsama.comet.objects.CometConfig
import io.github.starwishsama.comet.objects.group.PerGroupConfig
import io.github.starwishsama.comet.utils.*
import io.github.starwishsama.comet.utils.network.isUsable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import net.mamoe.mirai.Bot
import net.mamoe.yamlkt.Yaml
import java.io.File
import java.net.Socket

object DataSetup {
    private val userCfg: File = File(BotVariables.filePath, "users.json")
    private val shopItemCfg: File = File(BotVariables.filePath, "items.json")
    private val cfgFile: File = File(BotVariables.filePath, "config.yml")
    private val langCfg: File = File(BotVariables.filePath, "lang.json")
    private val cacheCfg: File = File(BotVariables.filePath, "cache.json")
    private val pcrData = File(FileUtil.getResourceFolder(), "pcr.json")
    private val arkNightData = File(FileUtil.getResourceFolder(), "arkNights.json")
    private val perGroupFolder = FileUtil.getChildFolder("groups")

    fun init() {
        if (!userCfg.exists() || !cfgFile.exists()) {
            try {
                cfgFile.writeString(Yaml.default.encodeToString(CometConfig()))
                userCfg.writeClassToJson(BotVariables.users)
                shopItemCfg.writeClassToJson(BotVariables.shop)
                println("[配置] 已自动生成新的配置文件.")
            } catch (e: RuntimeException) {
                daemonLogger.warning("[配置] 在生成配置文件时发生了错误", e)
            }
        }

        load()
    }

    private fun saveCfg() {
        try {
            cfgFile.writeString(Yaml.default.encodeToString(CometConfig.serializer(), cfg))
            userCfg.writeClassToJson(BotVariables.users)
            shopItemCfg.writeClassToJson(BotVariables.shop)
            savePerGroupSetting()
            cacheCfg.writeClassToJson(BotVariables.cache)
        } catch (e: Exception) {
            daemonLogger.warning("[配置] 在保存配置文件时发生了问题", e)
        }
    }

    private fun load() {
        try {
            cfg = Yaml.default.decodeFromString(CometConfig.serializer(), cfgFile.getContext())
            BotVariables.users.addAll(gson.fromJson<List<BotUser>>(userCfg.getContext()))

            BotVariables.shop = gson.fromJson(shopItemCfg.getContext())

            loadLang()

            FileUtil.initResourceFile()

            if (pcrData.exists()) {
                BotVariables.pcr = gson.fromJson(pcrData.getContext())
                daemonLogger.info("成功载入公主连结游戏数据, 共 ${BotVariables.pcr.size} 个")
            } else {
                daemonLogger.info("未检测到公主连结游戏数据, 抽卡模拟器将无法使用")
            }

            if (arkNightData.exists()) {
                arkNight = gson.fromJson(arkNightData.getContext())

                daemonLogger.info("成功载入明日方舟游戏数据, 共 ${arkNight.size} 个")
                if (cfg.arkDrawUseImage) {
                    TaskUtil.runAsync {
                        runBlocking {
                            withContext(Dispatchers.IO) {
                                DrawUtil.downloadArkNightsFile()
                            }
                        }
                    }
                }
            } else {
                daemonLogger.info("未检测到明日方舟游戏数据, 抽卡模拟器将无法使用")
            }

            if (!cacheCfg.exists()) {
                cacheCfg.writeClassToJson(JsonObject())
            } else {
                BotVariables.cache = JsonParser.parseString(cacheCfg.getContext()).asJsonObject
            }

            daemonLogger.info("[配置] 成功载入配置文件")
        } catch (e: Exception) {
            daemonLogger.warning("[配置] 在加载配置文件时发生了问题", e)
        }
    }

    private fun loadLang() {
        if (!langCfg.exists()) {
            val default = arrayOf(BotLocalization("msg.bot-prefix", "Bot > "),
                    BotLocalization("msg.no-permission", "你没有权限"),
                    BotLocalization("msg.bind-success", "绑定账号 %s 成功!"),
                    BotLocalization("checkin.first-time", "你还没有签到过, 先用 /qd 签到一次吧~")
            )
            for (text in default) {
                BotVariables.localMessage.plusAssign(text)
            }
            langCfg.writeClassToJson(BotVariables.localMessage)
        } else {
            val lang: JsonElement =
                    JsonParser.parseString(langCfg.getContext())
            if (lang.isJsonArray) {
                BotVariables.localMessage = gson.fromJson(FileReader.create(langCfg).readString())
                daemonLogger.info("[配置] 成功载入多语言文件")
            } else {
                daemonLogger.warning("[配置] 在读取时发生了问题, 非法的 JSON 文件")
            }
        }
    }


    fun saveFiles() {
        daemonLogger.info("[数据] 自动保存数据完成")
        saveCfg()
        langCfg.writeClassToJson(BotVariables.localMessage)
        savePerGroupSetting()
    }

    fun reload() {
        // 仅重载配置文件
        cfg = Yaml.default.decodeFromString(CometConfig.serializer(), cfgFile.getContext())

        try {
            val socket = Socket(cfg.proxyUrl, cfg.proxyPort)
            socket.isUsable()
        } catch (e: Exception) {
            daemonLogger.verbose("无法连接到代理服务器, ${e.message}")
        }
    }

    fun initPerGroupSetting(bot: Bot) {
        if (!perGroupFolder.exists()) {
            perGroupFolder.mkdirs()
        }

        bot.groups.forEach {
            val loc = File(perGroupFolder, "${it.id}.json")
            if (!loc.exists()) {
                FileUtil.createBlankFile(loc)
                BotVariables.perGroup.add(PerGroupConfig(it.id))
            } else {
                try {
                    BotVariables.perGroup.add(loc.parseAsClass(PerGroupConfig::class.java))
                } catch (e: Exception) {
                    BotVariables.logger.warning("[配置] 在加载分群配置时出现了问题", e)
                }
            }
        }

        BotVariables.logger.info("[配置] 成功加载了 ${BotVariables.perGroup.size} 个群配置")
    }

    private fun savePerGroupSetting() {
        if (!perGroupFolder.exists()) return

        BotVariables.perGroup.forEach {
            val loc = File(perGroupFolder, "${it.id}.json")
            if (!loc.exists()) loc.createNewFile()
            loc.writeClassToJson(it)
        }
    }
}