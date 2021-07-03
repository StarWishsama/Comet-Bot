/*
 * Copyright (c) 2019-2021 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package io.github.starwishsama.comet.listeners

import com.fasterxml.jackson.databind.JsonNode
import io.github.starwishsama.comet.CometVariables
import io.github.starwishsama.comet.CometVariables.mapper
import io.github.starwishsama.comet.api.thirdparty.bilibili.VideoApi
import io.github.starwishsama.comet.utils.StringUtil
import io.github.starwishsama.comet.utils.json.isUsable
import io.github.starwishsama.comet.utils.network.NetUtil
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.isBotMuted
import net.mamoe.mirai.event.Event
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.EmptyMessageChain
import net.mamoe.mirai.message.data.LightApp
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.utils.MiraiExperimentalApi
import kotlin.time.ExperimentalTime

object ConvertLightAppListener : NListener {
    override val eventToListen = listOf(GroupMessageEvent::class)

    @MiraiExperimentalApi
    @ExperimentalTime
    override fun listen(event: Event) {
        if (event is GroupMessageEvent && !event.group.isBotMuted) {
            val lightApp = event.message[LightApp]
            if (lightApp != null) {
                val result = parseJsonMessage(lightApp, event.subject)
                if (result !is EmptyMessageChain) runBlocking { event.subject.sendMessage(result) }
            }
        }
    }

    private fun parseJsonMessage(lightApp: LightApp, subject: Contact): MessageChain {
        val cardJson = mapper.readTree(lightApp.content)
        if (cardJson.isUsable()) {
            val prompt = cardJson["prompt"].asText()
            if (prompt != null && prompt.contains("哔哩哔哩")) {
                return biliBiliCardConvert(cardJson["meta"]["detail_1"], subject)
            }
        }
        return EmptyMessageChain
    }

    private fun biliBiliCardConvert(meta: JsonNode?, subject: Contact): MessageChain {
        if (meta == null) return EmptyMessageChain

        return try {
            val url = meta["qqdocurl"].asText()

            val videoID = StringUtil.parseVideoIDFromBili(NetUtil.getRedirectedURL(url) ?: return EmptyMessageChain)

            val videoInfo = if (videoID.contains("BV")) {
                VideoApi.videoService.getVideoInfoByBID(videoID)
            } else {
                VideoApi.videoService.getVideoInfo(videoID)
            }.execute().body() ?: return EmptyMessageChain


            return runBlocking {
                val wrapper = videoInfo.toMessageWrapper()
                return@runBlocking if (!wrapper.isUsable()) {
                    EmptyMessageChain
                } else {
                    wrapper.toMessageChain(subject)
                }
            }
        } catch (e: Exception) {
            CometVariables.logger.warning("[监听器] 无法解析卡片消息", e)
            EmptyMessageChain
        }
    }

    override fun getName(): String = "去你大爷的小程序"
}