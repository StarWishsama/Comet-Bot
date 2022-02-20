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
import io.github.starwishsama.comet.api.thirdparty.bilibili.video.toMessageWrapper
import io.github.starwishsama.comet.managers.GroupConfigManager
import io.github.starwishsama.comet.objects.CometUser
import io.github.starwishsama.comet.utils.network.NetUtil
import io.github.starwishsama.comet.utils.serialize.isUsable
import kotlinx.coroutines.runBlocking
import moe.sdl.yabapi.data.video.VideoInfo
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.isBotMuted
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.EmptyMessageChain
import net.mamoe.mirai.message.data.LightApp
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.utils.MiraiExperimentalApi
import kotlin.time.ExperimentalTime

object BiliBiliShareListener : INListener {
    override val name: String
        get() = "哔哩哔哩解析"

    private val shortUrlPattern = Regex("""https://b23.tv/\w+""")
    private val longUrlPattern = Regex("""https://www.bilibili.com/video/(av|BV)\w+""")

    @OptIn(MiraiExperimentalApi::class, ExperimentalTime::class)
    @EventHandler
    fun listen(event: GroupMessageEvent) {
        if (!event.group.isBotMuted) {
            // Check parse feature is available
            if (GroupConfigManager.getConfig(event.group.id)?.canParseBiliVideo != true) {
                return
            }

            // First check if the message contains bilibili video url or light app card
            val targetURL = parseBiliBiliURL(event.message.contentToString())

            if (targetURL.isEmpty() && event.message.none { it is LightApp }) {
                return
            }

            if (CometUser.getUser(event.sender.id)?.isNoCoolDown() == true) {
                val resultChain = if (targetURL.isNotEmpty()) {
                    val checkResult = biliBiliLinkConvert(targetURL, event.subject)

                    checkResult.ifEmpty { EmptyMessageChain }
                } else {
                    val lightApp = event.message[LightApp] ?: return

                    val result = parseJsonMessage(lightApp, event.subject)
                    result.ifEmpty { EmptyMessageChain }
                }

                runBlocking { event.subject.sendMessage(resultChain) }
            }
        }
    }

    private fun parseBiliBiliURL(message: String): String {
        return shortUrlPattern.find(message)?.groups?.get(0)?.value
            ?: longUrlPattern.find(message)?.groups?.get(0)?.value
            ?: ""
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
            biliBiliLinkConvert(url, subject)
        } catch (e: Exception) {
            CometVariables.logger.warning("[监听器] 无法解析卡片消息", e)
            EmptyMessageChain
        }
    }

    private fun biliBiliLinkConvert(url: String, subject: Contact): MessageChain {
        val videoID = if (shortUrlPattern.matches(url)) {
            parseVideoIDFromBili(NetUtil.getRedirectedURL(url) ?: return EmptyMessageChain)
        } else {
            parseVideoIDFromBili(url)
        }

        val videoInfo: VideoInfo = runBlocking {
            return@runBlocking if (videoID.contains("BV")) {
                VideoApi.getVideoInfo(videoID)
            } else {
                VideoApi.getVideoInfo(videoID.lowercase().replace("av", "").toInt())
            }
        } ?: return EmptyMessageChain

        return runBlocking {
            val wrapper = videoInfo.toMessageWrapper()
            return@runBlocking if (!wrapper.isUsable()) {
                EmptyMessageChain
            } else {
                wrapper.toMessageChain(subject)
            }
        }
    }

    private fun parseVideoIDFromBili(url: String): String {
        val videoID = url.substring(0, if (url.indexOf("?") == -1) url.length else url.indexOf("?"))
            .replace("https", "")
            .replace("https", "")
            .split("/")
        return videoID.last()
    }
}
