package io.github.starwishsama.comet.file

import cn.hutool.core.io.file.FileReader
import com.charleskorn.kaml.Yaml
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import io.github.starwishsama.comet.BotVariables
import io.github.starwishsama.comet.BotVariables.gson
import io.github.starwishsama.comet.objects.BotLocalization
import io.github.starwishsama.comet.objects.BotUser
import io.github.starwishsama.comet.objects.Config
import io.github.starwishsama.comet.objects.draw.ArkNightOperator
import io.github.starwishsama.comet.objects.draw.PCRCharacter
import io.github.starwishsama.comet.objects.group.PerGroupConfig
import io.github.starwishsama.comet.objects.group.Shop
import io.github.starwishsama.comet.utils.*
import java.io.File

object DataSetup {
    private val userCfg: File = File(BotVariables.filePath, "users.json")
    private val shopItemCfg: File = File(BotVariables.filePath, "/items.json")
    private val cfgFile: File = File(BotVariables.filePath, "/config.yml")
    private val langCfg: File = File(BotVariables.filePath, "/lang.json")
    private val cacheCfg: File = File(BotVariables.filePath, "cache.json")
    private val pcrData = File(BotVariables.filePath, "/pcr.json")
    private val arkNightData = File(BotVariables.filePath, "/ark.json")

    private val perGroupFolder = FileUtil.getChildFolder("groups")

    fun init() {
        println("机器人文件路径在: " + BotVariables.filePath)
        if (!userCfg.exists() || !cfgFile.exists()) {
            try {
                cfgFile.writeString(Yaml.default.stringify(Config.serializer(), Config()))
                userCfg.writeClassToJson(BotVariables.users)
                shopItemCfg.writeClassToJson(BotVariables.shop)
                println("[配置] 已自动生成新的配置文件.")
            } catch (e: Exception) {
                System.err.println("[配置] 在生成配置文件时发生了错误")
                e.printStackTrace()
            }
        }

        load()
    }

    private fun saveCfg() {
        try {
            cfgFile.writeString(Yaml.default.stringify(Config.serializer(), BotVariables.cfg))
            userCfg.writeClassToJson(BotVariables.users)
            shopItemCfg.writeClassToJson(BotVariables.shop)
            savePerGroupSetting()
            cacheCfg.writeClassToJson(BotVariables.cache)
        } catch (e: Exception) {
            System.err.println("[配置] 在保存配置文件时发生了问题, 错误信息: ")
            e.printStackTrace()
        }
    }

    private fun load() {
        try {
            BotVariables.cfg = Yaml.default.parse(Config.serializer(), cfgFile.getContext())
            BotVariables.users = gson.fromJson(
                userCfg.getContext(),
                object : TypeToken<List<BotUser>>() {}.type
            )
            BotVariables.shop = gson.fromJson(
                shopItemCfg.getContext(),
                object : TypeToken<List<Shop>>() {}.type
            )

            loadLang()

            if (pcrData.exists()) {
                BotVariables.pcr = gson.fromJson(
                    pcrData.getContext(),
                    object : TypeToken<List<PCRCharacter>>() {}.type
                )
            }

            if (arkNightData.exists()) {
                BotVariables.arkNight = gson.fromJson(
                    arkNightData.getContext(),
                    object : TypeToken<List<ArkNightOperator>>() {}.type
                )
            }

            if (!cacheCfg.exists()) {
                val jsonObject = JsonObject()
                jsonObject.addProperty("token", "")
                jsonObject.addProperty("get_time", 0L)
                cacheCfg.writeClassToJson(jsonObject)
            } else {
                BotVariables.cache = JsonParser.parseString(cacheCfg.getContext()).asJsonObject
            }

            println("[配置] 成功载入配置文件")
        } catch (e: Exception) {
            System.err.println("[配置] 在加载配置文件时发生了问题, 错误")
            e.printStackTrace()
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
                BotVariables.localMessage = BotVariables.localMessage + text
            }
            langCfg.writeClassToJson(BotVariables.localMessage)
        } else {
            val lang: JsonElement =
                    JsonParser.parseString(langCfg.getContext())
            if (lang.isJsonArray) {
                BotVariables.localMessage = gson.fromJson(
                    FileReader.create(langCfg).readString(),
                    object : TypeToken<List<BotLocalization>>() {}.type
                )
                println("[配置] 成功载入多语言文件")
            } else {
                System.err.println("[配置] 在读取时发生了问题, 非法的 JSON 文件")
            }
        }
    }

    private fun saveLang() {
        langCfg.writeClassToJson(BotVariables.localMessage)
    }

    fun saveFiles() {
        BotVariables.logger.info("[Bot] 自动保存数据完成")
        saveCfg()
        saveLang()
        savePerGroupSetting()
    }

    fun reload() {
        // 仅重载配置文件
        BotVariables.cfg = cfgFile.parseAsClass(Config::class.java)
    }

    fun initPerGroupSetting() {
        if (!perGroupFolder.exists()) {
            perGroupFolder.mkdirs()
        }

        var count = 0

        BotVariables.bot.groups.forEach {
            val loc = File(perGroupFolder, "${it.id}.json")
            if (!loc.exists()) {
                FileUtil.createBlankFile(loc)
                BotVariables.perGroup.add(PerGroupConfig(it.id))
                count++
            } else {
                try {
                    BotVariables.perGroup.add(loc.parseAsClass(PerGroupConfig::class.java))
                    count++
                } catch (t: Throwable) {
                    BotVariables.logger.warning("[配置] 在加载分群配置时出现了问题", t)
                }
            }
        }

        BotVariables.logger.info("[群配置] 成功加载了 $count 个群配置")
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