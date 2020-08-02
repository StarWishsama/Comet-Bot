package io.github.starwishsama.comet.listeners

import com.google.gson.JsonParser
import io.github.starwishsama.comet.BotVariables
import io.github.starwishsama.comet.utils.toMsgChain
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.isBotMuted
import net.mamoe.mirai.event.subscribeGroupMessages
import net.mamoe.mirai.message.data.EmptyMessageChain
import net.mamoe.mirai.message.data.LightApp
import net.mamoe.mirai.message.data.MessageChain
import kotlin.time.ExperimentalTime

object ConvertLightAppListener : NListener {
    @ExperimentalTime
    override fun register(bot: Bot) {
        bot.subscribeGroupMessages {
            always {
                if (BotVariables.switch && !this.group.isBotMuted) {
                    val lightApp = message[LightApp]
                    if (lightApp != null) {
                        val result = parseJsonMessage(lightApp)
                        if (result !is EmptyMessageChain) reply(result)
                    }
                }
            }
        }
    }

    private fun parseJsonMessage(lightApp: LightApp): MessageChain {
        val json = JsonParser.parseString(lightApp.content)
        if (json.isJsonObject) {
            val jsonObject = json.asJsonObject
            val prompt = jsonObject["prompt"].asString
            if (prompt != null && prompt.contentEquals("[QQ小程序]哔哩哔哩")) {
                val meta = jsonObject["meta"].asJsonObject["detail_1"].asJsonObject
                try {
                    if (meta != null) {
                        val title = meta["desc"].asString
                        val url = meta["qqdocurl"].asString
                        return (
                                "小程序Anti > 自动转换了小程序链接:\n" +
                                        "视频标题: $title\n" +
                                        "链接: ${url.substring(0, url.indexOf("?") - 1)}"
                                ).toMsgChain()
                    }
                } catch (e: IllegalStateException) {
                    BotVariables.logger.warning("[监听器] 无法解析卡片消息", e)
                    return EmptyMessageChain
                }
            }
        }
        return EmptyMessageChain
    }

    override fun getName(): String = "去你大爷的小程序"
}