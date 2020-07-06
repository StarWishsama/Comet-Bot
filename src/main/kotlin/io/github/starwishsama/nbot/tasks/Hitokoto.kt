package io.github.starwishsama.nbot.tasks

import com.github.salomonbrys.kotson.contains
import com.github.salomonbrys.kotson.set
import com.google.gson.JsonParser
import io.github.starwishsama.nbot.BotConstants
import io.github.starwishsama.nbot.BotMain
import io.github.starwishsama.nbot.utils.NetUtil

object Hitokoto : Runnable {
    override fun run() {
        try {
            val hitokotoJson = NetUtil.getPageContent("https://v1.hitokoto.cn/")
            val jsonObject = JsonParser.parseString(hitokotoJson)
            if (jsonObject.isJsonObject) {
                val cacheObject = BotConstants.cache
                if (cacheObject.contains("hitokoto")) {
                    val hitokotoCache = cacheObject.asJsonObject["hitokoto"].asString
                    if (jsonObject.asJsonObject["hitokoto"].asString != hitokotoCache) {
                        cacheObject["hitokoto"] = jsonObject
                    }
                } else {
                    cacheObject["hitokoto"] = jsonObject
                }
                BotMain.logger.warning("已获取到今日一言")
            }
        } catch (e: Throwable) {
            BotMain.logger.warning("在获取一言时发生了问题", e)
        }
    }
}