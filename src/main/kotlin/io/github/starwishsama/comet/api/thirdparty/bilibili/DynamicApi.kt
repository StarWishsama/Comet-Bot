/*
 * Copyright (c) 2019-2021 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package io.github.starwishsama.comet.api.thirdparty.bilibili

import cn.hutool.http.HttpRequest
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.module.kotlin.readValue
import io.github.starwishsama.comet.CometVariables.daemonLogger
import io.github.starwishsama.comet.CometVariables.mapper
import io.github.starwishsama.comet.api.thirdparty.ApiExecutor
import io.github.starwishsama.comet.api.thirdparty.bilibili.data.dynamic.Dynamic
import io.github.starwishsama.comet.api.thirdparty.bilibili.data.dynamic.DynamicData
import io.github.starwishsama.comet.api.thirdparty.bilibili.data.dynamic.DynamicTypeSelector
import io.github.starwishsama.comet.api.thirdparty.bilibili.data.dynamic.dynamicdata.UnknownType
import io.github.starwishsama.comet.exceptions.ApiException
import io.github.starwishsama.comet.exceptions.RateLimitException
import io.github.starwishsama.comet.objects.wrapper.MessageWrapper
import io.github.starwishsama.comet.utils.FileUtil
import io.github.starwishsama.comet.utils.network.NetUtil
import io.github.starwishsama.comet.utils.serialize.isUsable

/**
 * BiliBili 动态 API
 *
 * 获取用户的最新动态
 * 支持多种格式
 *
 * @author StarWishsama
 */
object DynamicApi : ApiExecutor {
    private const val dynamicUrl =
        "https://api.vc.bilibili.com/dynamic_svr/v1/dynamic_svr/space_history?host_uid=%uid%&offset_dynamic_id=0&need_top=0"
    private const val infoUrl = "https://api.bilibili.com/x/space/acc/info?mid="
    private const val dynamicByIdUrl =
        "https://api.vc.bilibili.com/dynamic_svr/v1/dynamic_svr/get_dynamic_detail?dynamic_id="
    private val agent = mutableMapOf("User-Agent" to "Nameless live status checker by StarWishsama")
    private const val apiRateLimit = "BiliBili API调用已达上限"

    private val cacheDynamicData = mutableMapOf<DynamicData, Dynamic>()
    private val cacheDynamic = mutableMapOf<Long, Dynamic>()

    @Throws(RateLimitException::class)
    fun getUserNameByMid(mid: Long): String {
        if (isReachLimit()) {
            throw RateLimitException(apiRateLimit)
        }

        val response = HttpRequest.get(infoUrl + mid).timeout(2000)
            .addHeaders(agent)
            .executeAsync()
        return mapper.readTree(response.body())["data"]["name"].asText()
    }

    @Throws(ApiException::class)
    fun getDynamicById(id: Long): Dynamic {
        val cache = getCacheDynamic(id)

        if (cache != null) {
            return cache
        }

        if (isReachLimit()) {
            throw RateLimitException(apiRateLimit)
        }

        NetUtil.executeHttpRequest(
            url = dynamicByIdUrl + id.toString()
        ).use { res ->
            if (res.isSuccessful) {
                val body = res.body?.string() ?: throw ApiException("无法获取动态页面")
                val result: Dynamic = mapper.readValue(body)
                cacheDynamic[result.getDynamicID()] = result
                return result
            } else {
                throw ApiException("无法获取动态页面, 状态码 ${res.code}")
            }
        }
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    @Throws(ApiException::class)
    fun getUserDynamicTimeline(mid: Long): Dynamic? {
        if (isReachLimit()) {
            throw RateLimitException(apiRateLimit)
        }

        val response = NetUtil.executeHttpRequest(
            url = dynamicUrl.replace("%uid%", mid.toString())
        )

        val url = response.request.url.toString()
        var body = ""
        try {
            if (response.isSuccessful) {
                body = response.body?.string() ?: return null
                val result: Dynamic = mapper.readValue(body)
                cacheDynamic[result.getDynamicID()] = result
                return result
            } else {
                daemonLogger.warning("解析动态时出现异常")
                return null
            }
        } catch (e: Exception) {
            if (e is JsonProcessingException) {
                FileUtil.createErrorReportFile("解析动态失败", "bilibili", e, body, "请求 URL 为: $url")
                throw ApiException("解析动态失败, 无法解析传入 Json")
            } else {
                daemonLogger.warning("解析动态时出现异常", e)
            }

            return null
        }
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    @Throws(ApiException::class)
    fun getWrappedDynamicTimeline(mid: Long): MessageWrapper {
        val dynamic = getUserDynamicTimeline(mid) ?: return MessageWrapper().addText("无法获取动态").setUsable(false)

        try {
            if (dynamic.data.cards != null) {
                if (dynamic.data.cards.isEmpty()) return MessageWrapper().addText("没有发过动态").setUsable(false)

                val card = dynamic.data.cards[0]
                val singleDynamicObject = mapper.readTree(card.card)

                if (singleDynamicObject.isUsable()) {
                    val dynamicType = DynamicTypeSelector.getType(card.description.type)
                    return if (dynamicType != UnknownType::class) {
                        val dynamicData = mapper.readValue(card.card, dynamicType)
                        insertDynamicData(dynamic, dynamicData)
                        return dynamicData.asMessageWrapper()
                    } else {
                        MessageWrapper().addText("错误: 不支持的动态类型").setUsable(false)
                    }
                }
            }

            return MessageWrapper().addText("获取动态失败").setUsable(false)
        } catch (e: Exception) {
            return MessageWrapper().addText("解析动态失败").setUsable(false)
        }
    }

    fun insertDynamicData(dynamic: Dynamic, data: DynamicData) {
        cacheDynamicData[data] = dynamic
    }

    fun getDynamicByData(data: DynamicData): Dynamic? {
        val result = cacheDynamicData[data]

        if (result != null) {
            cacheDynamicData.remove(data)
        }

        return result
    }

    fun getCacheDynamic(id: Long): Dynamic? = cacheDynamic[id]

    override var usedTime: Int = 0
    override val duration: Int = 3

    override fun isReachLimit(): Boolean {
        val result = usedTime > getLimitTime()
        if (!result) usedTime++
        return result
    }

    override fun getLimitTime(): Int = 10000
}