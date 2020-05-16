package io.github.starwishsama.nbot.file

import cn.hutool.core.io.file.FileReader
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import io.github.starwishsama.nbot.BotConstants
import io.github.starwishsama.nbot.BotInstance
import io.github.starwishsama.nbot.managers.GroupConfigManager
import io.github.starwishsama.nbot.objects.BotLocalization
import io.github.starwishsama.nbot.objects.BotUser
import io.github.starwishsama.nbot.objects.Config
import io.github.starwishsama.nbot.objects.draw.ArkNightOperator
import io.github.starwishsama.nbot.objects.draw.PCRCharacter
import io.github.starwishsama.nbot.objects.group.GroupConfig
import io.github.starwishsama.nbot.objects.group.Shop
import io.github.starwishsama.nbot.util.getContext
import io.github.starwishsama.nbot.util.initConfig
import io.github.starwishsama.nbot.util.writeJson
import java.io.File

object DataSetup {
    private val userCfg: File = File(BotInstance.filePath.toString(), "users.json")
    private val shopItemCfg: File = File(BotInstance.filePath.toString(), "/items.json")
    private val cfgFile: File = File(BotInstance.filePath.toString(), "/config.json")
    private val langCfg: File = File(BotInstance.filePath.toString(), "/lang.json")
    private val groupCfg: File = File(BotInstance.filePath.toString(), "/groups.json")
    private val cacheCfg: File = File(BotInstance.filePath.toString(), "cache.json")
    private val pcrData = File(BotInstance.filePath.toString(), "/pcr.json")
    private val arkNightData = File(BotInstance.filePath.toString(), "/ark.json")
    private val gson = BotConstants.gson

    fun initData() {
        if (!userCfg.exists() || !cfgFile.exists()) {
            try {
                cfgFile.initConfig(BotConstants.cfg)
                userCfg.initConfig(BotConstants.users)
                shopItemCfg.initConfig(BotConstants.shop)
                groupCfg.initConfig(GroupConfigManager.configs)
                println("[配置] 已自动生成新的配置文件.")
            } catch (e: Exception) {
                System.err.println("[配置] 在生成配置文件时发生了错误, 错误信息: " + e.message)
            }
        }

        load()
    }

    private fun saveCfg() {
        try {
            cfgFile.writeJson(BotConstants.cfg)
            userCfg.writeJson(BotConstants.users)
            shopItemCfg.writeJson(BotConstants.shop)
            groupCfg.writeJson(GroupConfigManager.configs)
            cacheCfg.writeJson(BotConstants.cache)
        } catch (e: Exception) {
            System.err.println("[配置] 在保存配置文件时发生了问题, 错误信息: ")
            e.printStackTrace()
        }
    }

    private fun load() {
        try {
            BotConstants.cfg = gson.fromJson(cfgFile.getContext(), Config::class.java) as Config
            BotConstants.users = gson.fromJson(
                    userCfg.getContext(),
                    object : TypeToken<List<BotUser>>() {}.type
            )
            BotConstants.shop = gson.fromJson(
                    shopItemCfg.getContext(),
                    object : TypeToken<List<Shop>>() {}.type
            )
            GroupConfigManager.configs = gson.fromJson(
                    groupCfg.getContext(),
                    object : TypeToken<Map<Long, GroupConfig>>() {}.type
            )

            loadLang()

            BotConstants.pcr = gson.fromJson(pcrData.getContext(),
                    object : TypeToken<List<PCRCharacter>>() {}.type
            )
            BotConstants.arkNight = gson.fromJson(arkNightData.getContext(),
                    object : TypeToken<List<ArkNightOperator>>() {}.type
            )

            if (!cacheCfg.exists()) {
                val jsonObject = JsonObject()
                jsonObject.addProperty("token", "")
                jsonObject.addProperty("get_time", 0L)
                cacheCfg.writeJson(jsonObject)
            } else {
                BotConstants.cache = JsonParser.parseString(cacheCfg.getContext()).asJsonObject
            }

            println("[配置] 成功载入配置文件")
        } catch (e: Exception) {
            System.err.println("[配置] 在加载配置文件时发生了问题, 错误信息: $e")
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
                BotConstants.msg = BotConstants.msg + text
            }
            langCfg.writeJson(BotConstants.msg)
        } else {
            val lang: JsonElement =
                    JsonParser.parseString(langCfg.getContext())
            if (lang.isJsonArray) {
                BotConstants.msg = gson.fromJson(FileReader.create(langCfg).readString(),
                        object : TypeToken<List<BotLocalization>>() {}.type
                )
                println("[配置] 成功载入多语言文件")
            } else {
                System.err.println("[配置] 在读取时发生了问题, 非法的 JSON 文件")
            }
        }
    }

    private fun saveLang() {
        langCfg.writeJson(BotConstants.msg)
    }

    fun saveFiles() {
        BotInstance.logger.info("[Bot] 自动保存数据完成")
        saveCfg()
        saveLang()
    }

    fun reload() {
        load()
    }
}