package io.github.starwishsama.nbot.config

import cn.hutool.core.io.file.FileReader
import cn.hutool.core.io.file.FileWriter
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import io.github.starwishsama.nbot.BotConstants
import io.github.starwishsama.nbot.BotInstance
import io.github.starwishsama.nbot.managers.GroupConfigManager
import io.github.starwishsama.nbot.objects.*
import io.github.starwishsama.nbot.objects.draw.ArkNightOperator
import io.github.starwishsama.nbot.objects.draw.PCRCharacter
import io.github.starwishsama.nbot.objects.group.GroupConfig
import io.github.starwishsama.nbot.objects.group.GroupShop
import java.io.File

object DataSetup {
    private val userCfg: File = File(BotInstance.filePath.toString(),"users.json")
    private val shopItemCfg: File = File(BotInstance.filePath.toString(),"/items.json")
    private val cfgFile: File = File(BotInstance.filePath.toString(),"/config.json")
    private val langCfg: File = File(BotInstance.filePath.toString(), "/lang.json")
    private val groupCfg: File = File(BotInstance.filePath.toString(), "/groups.json")
    private val gson = GsonBuilder().serializeNulls().setPrettyPrinting().create()

    fun loadCfg() {
        if (BotInstance.filePath != null) {
            if (userCfg.exists() && cfgFile.exists()) {
                load()
            } else {
                try {
                    BotConstants.cfg.autoSaveTime = 15
                    FileWriter.create(cfgFile).write(gson.toJson(BotConstants.cfg))
                    FileWriter.create(userCfg).write(gson.toJson(BotConstants.users))
                    FileWriter.create(shopItemCfg).write(gson.toJson(BotConstants.shop))
                    FileWriter.create(groupCfg).write(gson.toJson(GroupConfigManager.configs))
                    load()
                    println("[配置] 已自动生成新的配置文件.")
                } catch (e: Exception) {
                    System.err.println("[配置] 在生成配置文件时发生了错误, 错误信息: " + e.message)
                }
            }
        }
    }

    private fun saveCfg() {
        try {
            FileWriter.create(cfgFile).write(gson.toJson(BotConstants.cfg))
            FileWriter.create(userCfg).write(gson.toJson(BotConstants.users))
            FileWriter.create(shopItemCfg).write(gson.toJson(BotConstants.shop))
            FileWriter.create(groupCfg).write(gson.toJson(GroupConfigManager.configs))
        } catch (e: Exception) {
            System.err.println("[配置] 在保存配置文件时发生了问题, 错误信息: ")
            e.printStackTrace()
        }
    }


    private fun load() {
        try {
            val userContent: String = FileReader.create(userCfg).readString()
            val configContent: String = FileReader.create(cfgFile).readString()
            val groupContent: String = FileReader.create(groupCfg).readString()
            val checkInParser: JsonElement = JsonParser.parseString(userContent)
            val configParser: JsonElement = JsonParser.parseString(configContent)
            if (!checkInParser.isJsonNull && !configParser.isJsonNull) {
                BotConstants.cfg = gson.fromJson(configContent, Config::class.java) as Config
                BotConstants.users = gson.fromJson(
                    userContent,
                    object : TypeToken<List<BotUser>>() {}.type
                )
                BotConstants.shop = gson.fromJson(
                    FileReader.create(shopItemCfg).readString(),
                    object : TypeToken<List<GroupShop>>() {}.type
                )
                GroupConfigManager.configs = gson.fromJson(
                        groupContent,
                        object : TypeToken<Map<Long, GroupConfig>>() {}.type
                )
                loadLang()
            } else {
                System.err.println("[配置] 在加载配置文件时发生了问题, JSON 文件为空.")
            }

            BotConstants.pcr = gson.fromJson(FileReader.create(File(BotInstance.filePath.toString(), "/pcr.json")).readString(), object : TypeToken<List<PCRCharacter>>() {}.type)
            BotConstants.arkNight = gson.fromJson(FileReader.create(File(BotInstance.filePath.toString(), "/ark.json")).readString(), object : TypeToken<List<ArkNightOperator>>() {}.type)

        } catch (e: Exception) {
            System.err.println("[配置] 在加载配置文件时发生了问题, 错误信息: $e")
        }
    }

    fun loadLang() {
        if (!langCfg.exists() && BotConstants.msg.isEmpty()) {
            BotConstants.msg = BotConstants.msg + BotLocalization("msg.bot-prefix", "Bot > ")
            BotConstants.msg = BotConstants.msg + BotLocalization("msg.no-permission", "你没有权限")
            BotConstants.msg = BotConstants.msg + BotLocalization("msg.bind-success", "绑定账号 %s 成功!")
            BotConstants.msg = BotConstants.msg + BotLocalization("checkin.first-time", "你还没有签到过, 先用 /qd 签到一次吧~")
            FileWriter.create(langCfg).write(gson.toJson(BotConstants.msg))
        } else {
            val lang: JsonElement =
                JsonParser.parseString(FileReader.create(langCfg).readString())
            if (!lang.isJsonNull) {
                BotConstants.msg = gson.fromJson(FileReader.create(langCfg).readString(), object : TypeToken<List<BotLocalization>>() {}.type
                )
            } else System.err.println("[配置] 在读取时发生了问题, JSON 文件为空")
        }
    }

    private fun saveLang() {
        FileWriter.create(langCfg).write(gson.toJson(BotConstants.msg))
    }

    fun saveFiles() {
        BotInstance.logger.info("[Bot] 自动保存数据完成")
        saveCfg()
        saveLang()
    }
}