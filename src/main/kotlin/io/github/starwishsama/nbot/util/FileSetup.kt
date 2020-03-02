package io.github.starwishsama.nbot.util

import cn.hutool.core.io.file.FileReader
import cn.hutool.core.io.file.FileWriter
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import io.github.starwishsama.nbot.BotConstants
import io.github.starwishsama.nbot.BotInstance
import io.github.starwishsama.nbot.objects.BotLocalization
import io.github.starwishsama.nbot.objects.BotUser
import io.github.starwishsama.nbot.objects.Config
import io.github.starwishsama.nbot.objects.ShopItem
import java.io.File


object FileSetup {
    private val userCfg: File = File(BotInstance().filePath.toString() + "/users.json")
    private val shopItemCfg: File = File(BotInstance().filePath.toString() + "/items.json")
    private val cfgFile: File = File(BotInstance().filePath.toString() + "/config.json")
    private val langCfg: File = File(BotInstance().filePath.toString() + "/lang.json")
    private val rssTemp: File = File(BotInstance().filePath.toString() + "/temp.txt")
    private val groupCfg: File = File(BotInstance().filePath.toString() + "/groups.json")
    private val gson = GsonBuilder().serializeNulls().create()

    fun loadCfg(constants: BotConstants) {
        if (BotInstance().filePath != null) {
            if (userCfg.exists() && cfgFile.exists()) {
               load(constants)
            } else {
                try {
                    constants.cfg.autoSaveTime = 15
                    constants.cfg.botId = 123456
                    constants.cfg.botPassword = "password"
                    FileWriter.create(cfgFile).write(gson.toJson(constants.cfg))
                    FileWriter.create(userCfg).write(gson.toJson(constants.users))
                    FileWriter.create(shopItemCfg).write(gson.toJson(constants.shopItems))
                    //FileWriter.create(groupCfg).write(gson.toJson(GroupConfigManager.getConfigMap()))
                    if (!rssTemp.createNewFile()) {
                        println("[配置] 缓存文件已存在, 已自动忽略.")
                    }
                    load(constants)
                    println("[配置] 已自动生成新的配置文件.")
                } catch (e: Exception) {
                    System.err.println("[配置] 在生成配置文件时发生了错误, 错误信息: " + e.message)
                }
            }
        }
    }

    fun saveCfg(constants: BotConstants) {
        try {
            FileWriter.create(cfgFile).write(gson.toJson(constants.cfg))
            FileWriter.create(userCfg).write(gson.toJson(constants.users))
            FileWriter.create(shopItemCfg).write(gson.toJson(constants.shopItems))
            //FileWriter.create(groupCfg).write(gson.toJson(GroupConfigManager.getConfigMap()))
        } catch (e: Exception) {
            System.err.println("[配置] 在保存配置文件时发生了问题, 错误信息: ")
            e.printStackTrace()
        }
    }


    private fun load(constants: BotConstants) {
        try {
            val userContent: String = FileReader.create(userCfg).readString()
            val configContent: String = FileReader.create(cfgFile).readString()
            //val groupContent: String = FileReader.create(groupCfg).readString()
            val checkInParser: JsonElement = JsonParser.parseString(userContent)
            val configParser: JsonElement = JsonParser.parseString(configContent)
            if (!checkInParser.isJsonNull && !configParser.isJsonNull) {
                constants.cfg = gson.fromJson<Any>(configContent, Config::class.java) as Config
                constants.users = gson.fromJson<Any>(
                    userContent,
                    object : TypeToken<Collection<BotUser?>?>() {}.type
                ) as Collection<BotUser>
                constants.shopItems = gson.fromJson(
                    FileReader.create(shopItemCfg).readString(),
                    object : TypeToken<Collection<ShopItem?>?>() {}.type
                )
                /**constants.GroupConfigManager.setConfigMap(
                    gson.fromJson(
                        groupContent,
                        object : TypeToken<Map<Long?, GroupConfig?>?>() {}.type
                    )
                ) */
            } else {
                System.err.println("[配置] 在加载配置文件时发生了问题, JSON 文件为空.")
            }
        } catch (e: Exception) {
            System.err.println("[配置] 在加载配置文件时发生了问题, 错误信息: $e")
        }
    }

    fun loadLang(constants: BotConstants) {
        if (!langCfg.exists()) {
            constants.msg = constants.msg + BotLocalization("msg.bot-prefix", "Bot > ")
            constants.msg = constants.msg + BotLocalization("msg.no-permission", "你没有权限")
            constants.msg = constants.msg + BotLocalization("msg.bind-success", "绑定账号 %s 成功!")
            constants.msg = constants.msg + BotLocalization("checkin.first-time", "你还没有签到过, 先用 /qd 签到一次吧~")
            FileWriter.create(langCfg).write(gson.toJson(constants.msg))
        } else {
            val lang: JsonElement =
                JsonParser.parseString(FileReader.create(langCfg).readString())
            if (!lang.isJsonNull) {
                constants.msg = gson.fromJson(
                    FileReader.create(langCfg).readString(),
                    object : TypeToken<List<BotLocalization?>?>() {}.type
                )
            } else System.err.println("[配置] 在读取时发生了问题, JSON 文件为空")
        }
    }

    fun saveLang(constants: BotConstants) {
        FileWriter.create(langCfg).write(gson.toJson(constants.msg))
    }

    fun saveFiles(constants: BotConstants) {
        BotInstance().logger.info("[Bot] 自动保存数据完成")
        saveCfg(constants)
        saveLang(constants)
    }
}