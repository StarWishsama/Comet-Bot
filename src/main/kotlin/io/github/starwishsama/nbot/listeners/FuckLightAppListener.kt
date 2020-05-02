package io.github.starwishsama.nbot.listeners

import com.google.gson.JsonParser
import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.subscribeGroupMessages
import net.mamoe.mirai.message.data.LightApp

object FuckLightAppListener : NListener {
    override fun register(bot: Bot) {
        bot.subscribeGroupMessages {
            always {
                try {
                    val lightApp = this[LightApp]
                    val json = JsonParser.parseString(lightApp.content)
                    if (json.isJsonObject) {
                        val jsonObject = json.asJsonObject
                        val prompt = jsonObject["prompt"].asString
                        if (prompt != null && prompt.contentEquals("[QQ小程序]哔哩哔哩")) {
                            val meta = jsonObject["meta"].asJsonObject["detail_1"].asJsonObject
                            if (meta != null) {
                                val title = meta["desc"].asString
                                val url = meta["qqdocurl"].asString
                                reply("小程序Anti > 自动为电脑选手转换了小程序:\n" +
                                        "视频标题: $title\n" +
                                        "链接: ${url.substring(0, url.indexOf("?") - 1)}")
                            }
                        }
                    }
                } catch (ignored: NoSuchElementException) {
                }
            }
        }
    }

    override fun getName(): String = "去你大爷的小程序"
}