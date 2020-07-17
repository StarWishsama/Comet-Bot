package io.github.starwishsama.comet.listeners

import com.google.gson.JsonParser
import io.github.starwishsama.comet.utils.toMirai
import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.subscribeGroupMessages
import net.mamoe.mirai.message.data.EmptyMessageChain
import net.mamoe.mirai.message.data.LightApp
import net.mamoe.mirai.message.data.MessageChain

object ConvertLightAppListener : NListener {
    override fun register(bot: Bot) {
        bot.subscribeGroupMessages {
            always {
                val lightApp = message[LightApp]
                if (lightApp != null) {
                    val result = parseCard(lightApp)
                    if (result !is EmptyMessageChain) reply(parseCard(lightApp))
                }
            }
        }
    }

    private fun parseCard(lightApp: LightApp): MessageChain {
        val json = JsonParser.parseString(lightApp.content)
        if (json.isJsonObject) {
            val jsonObject = json.asJsonObject
            val prompt = jsonObject["prompt"].asString
            if (prompt != null && prompt.contentEquals("[QQ小程序]哔哩哔哩")) {
                val meta = jsonObject["meta"].asJsonObject["detail_1"].asJsonObject
                if (meta != null) {
                    val title = meta["desc"].asString
                    val url = meta["qqdocurl"].asString
                    return (
                            "小程序Anti > 自动转换了小程序链接:\n" +
                                    "视频标题: $title\n" +
                                    "链接: ${url.substring(0, url.indexOf("?") - 1)}"
                            ).toMirai()
                }
            }
        }
        return EmptyMessageChain
    }

    override fun getName(): String = "去你大爷的小程序"
}