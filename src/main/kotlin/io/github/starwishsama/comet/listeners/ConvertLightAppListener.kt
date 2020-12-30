package io.github.starwishsama.comet.listeners

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import io.github.starwishsama.comet.BotVariables
import io.github.starwishsama.comet.api.thirdparty.bilibili.VideoApi
import io.github.starwishsama.comet.utils.StringUtil
import io.github.starwishsama.comet.utils.network.NetUtil
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.isBotMuted
import net.mamoe.mirai.event.globalEventChannel
import net.mamoe.mirai.event.subscribeGroupMessages
import net.mamoe.mirai.message.data.EmptyMessageChain
import net.mamoe.mirai.message.data.LightApp
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.utils.MiraiExperimentalApi
import kotlin.time.ExperimentalTime

object ConvertLightAppListener : NListener {
    @MiraiExperimentalApi
    @ExperimentalTime
    override fun register(bot: Bot) {
        bot.globalEventChannel().subscribeGroupMessages {
            always {
                if (BotVariables.switch && !this.group.isBotMuted) {
                    val lightApp = message[LightApp]
                    if (lightApp != null) {
                        val result = parseJsonMessage(lightApp, subject)
                        if (result !is EmptyMessageChain) subject.sendMessage(result)
                    }
                }
            }
        }
    }

    private fun parseJsonMessage(lightApp: LightApp, subject: Contact): MessageChain {
        val json = JsonParser.parseString(lightApp.content)
        if (json.isJsonObject) {
            val jsonObject = json.asJsonObject
            val prompt = jsonObject["prompt"].asString
            if (prompt != null && prompt.contentEquals("[QQ小程序]哔哩哔哩")) {
                return when {
                    prompt.contains("哔哩哔哩") -> biliBiliCardConvert(jsonObject["meta"].asJsonObject["detail_1"].asJsonObject, subject)
                    else -> EmptyMessageChain
                }
            }
        }
        return EmptyMessageChain
    }

    private fun biliBiliCardConvert(meta: JsonObject?, subject: Contact): MessageChain {
        if (meta == null) return EmptyMessageChain

        return try {
            val url = meta["qqdocurl"].asString

            val videoID = StringUtil.parseVideoIDFromBili(NetUtil.getRedirectedURL(url))
            val videoInfo = if (videoID.contains("bv")) {
                VideoApi.videoService.getVideoInfoByBID(videoID)
            } else {
                VideoApi.videoService.getVideoInfo(videoID)
            }.execute().body() ?: return EmptyMessageChain


            return runBlocking { videoInfo.toMessageWrapper().toMessageChain(subject, true) }
        } catch (e: IllegalStateException) {
            BotVariables.logger.warning("[监听器] 无法解析卡片消息", e)
            EmptyMessageChain
        }
    }

    override fun getName(): String = "去你大爷的小程序"
}