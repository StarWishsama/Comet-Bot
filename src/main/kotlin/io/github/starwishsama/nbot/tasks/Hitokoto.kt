package io.github.starwishsama.nbot.tasks

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
                val hitokoto = cacheObject.asJsonObject["hitokoto"].asString
                if (cacheObject["hitokoto"] != null && cacheObject["hitokoto"].asJsonObject["hitokoto"].asString != hitokoto) {
                    cacheObject.add("hitokoto", jsonObject)
                }
            }
        } catch (e: Throwable) {
            BotMain.logger.warning("无法连接至一言 API 服务器", e)
        }
    }
}