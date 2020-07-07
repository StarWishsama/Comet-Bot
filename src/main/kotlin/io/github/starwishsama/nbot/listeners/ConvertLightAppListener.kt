package io.github.starwishsama.nbot.listeners

import com.google.gson.JsonParser
import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.subscribeGroupMessages
import net.mamoe.mirai.message.data.LightApp

object ConvertLightAppListener : NListener {
    override fun register(bot: Bot) {
        bot.subscribeGroupMessages {
            always {
                val lightApp = message[LightApp]
                if (lightApp != null) {
                    val json = JsonParser.parseString(lightApp.content)
                    if (json.isJsonObject) {
                        val jsonObject = json.asJsonObject
                        val prompt = jsonObject["prompt"].asString
                        if (prompt != null && prompt.contentEquals("[QQ小程序]哔哩哔哩")) {
                            val meta = jsonObject["meta"].asJsonObject["detail_1"].asJsonObject
                            if (meta != null) {
                                val title = meta["desc"].asString
                                val url = meta["qqdocurl"].asString
                                reply(
                                    "小程序Anti > 自动转换了小程序链接:\n" +
                                            "视频标题: $title\n" +
                                            "链接: ${url.substring(0, url.indexOf("?") - 1)}"
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    override fun getName(): String = "去你大爷的小程序"
}