package io.github.starwishsama.comet.file

import cn.hutool.core.io.file.FileReader
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import io.github.starwishsama.comet.BotVariables
import io.github.starwishsama.comet.BotVariables.gson
import io.github.starwishsama.comet.Comet
import io.github.starwishsama.comet.managers.GroupConfigManager
import io.github.starwishsama.comet.objects.BotLocalization
import io.github.starwishsama.comet.objects.BotUser
import io.github.starwishsama.comet.objects.Config
import io.github.starwishsama.comet.objects.draw.ArkNightOperator
import io.github.starwishsama.comet.objects.draw.PCRCharacter
import io.github.starwishsama.comet.objects.group.GroupConfig
import io.github.starwishsama.comet.objects.group.Shop
import io.github.starwishsama.comet.utils.getContext
import io.github.starwishsama.comet.utils.writeJson
import java.io.File

object DataSetup {
    private val userCfg: File = File(Comet.filePath.toString(), "users.json")
    private val shopItemCfg: File = File(Comet.filePath.toString(), "/items.json")
    private val cfgFile: File = File(Comet.filePath.toString(), "/config.json")
    private val langCfg: File = File(Comet.filePath.toString(), "/lang.json")
    private val groupCfg: File = File(Comet.filePath.toString(), "/groups.json")
    private val cacheCfg: File = File(Comet.filePath.toString(), "cache.json")
    private val pcrData = File(Comet.filePath.toString(), "/pcr.json")
    private val arkNightData = File(Comet.filePath.toString(), "/ark.json")

    fun initData() {
        if (!Comet.log.exists()) {
            Comet.log.mkdirs()
        }

        if (!userCfg.exists() || !cfgFile.exists()) {
            try {
                cfgFile.writeJson(BotVariables.cfg)
                userCfg.writeJson(BotVariables.users)
                shopItemCfg.writeJson(BotVariables.shop)
                groupCfg.writeJson(GroupConfigManager.configs)
                println("[配置] 已自动生成新的配置文件.")
            } catch (e: Exception) {
                System.err.println("[配置] 在生成配置文件时发生了错误, 错误信息: " + e.message)
            }
        }

        load()
    }

    private fun saveCfg() {
        try {
            cfgFile.writeJson(BotVariables.cfg)
            userCfg.writeJson(BotVariables.users)
            shopItemCfg.writeJson(BotVariables.shop)
            groupCfg.writeJson(GroupConfigManager.configs)
            cacheCfg.writeJson(BotVariables.cache)
        } catch (e: Exception) {
            System.err.println("[配置] 在保存配置文件时发生了问题, 错误信息: ")
            e.printStackTrace()
        }
    }

    private fun load() {
        try {
            BotVariables.cfg = gson.fromJson(cfgFile.getContext(), Config::class.java)
            BotVariables.users = gson.fromJson(
                userCfg.getContext(),
                object : TypeToken<List<BotUser>>() {}.type
            )
            BotVariables.shop = gson.fromJson(
                shopItemCfg.getContext(),
                object : TypeToken<List<Shop>>() {}.type
            )
            GroupConfigManager.configs = gson.fromJson(
                groupCfg.getContext(),
                object : TypeToken<Map<Long, GroupConfig>>() {}.type
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
                cacheCfg.writeJson(jsonObject)
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
            langCfg.writeJson(BotVariables.localMessage)
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
        langCfg.writeJson(BotVariables.localMessage)
    }

    fun saveFiles() {
        Comet.logger.info("[Bot] 自动保存数据完成")
        saveCfg()
        saveLang()
    }

    fun reload() {
        // 仅重载配置文件
        BotVariables.cfg = gson.fromJson(cfgFile.getContext(), Config::class.java)
    }
}