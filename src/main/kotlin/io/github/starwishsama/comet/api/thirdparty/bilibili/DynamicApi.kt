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

import io.github.starwishsama.comet.CometVariables.daemonLogger
import io.github.starwishsama.comet.api.thirdparty.ApiExecutor
import io.github.starwishsama.comet.exceptions.ApiException
import io.github.starwishsama.comet.exceptions.RateLimitException
import io.github.starwishsama.comet.utils.FileUtil
import kotlinx.serialization.SerializationException
import moe.sdl.yabapi.api.getFeedByUid
import moe.sdl.yabapi.api.getFeedContent
import moe.sdl.yabapi.data.GeneralCode
import moe.sdl.yabapi.data.feed.FeedCardNode

/**
 * BiliBili 动态 API
 *
 * 获取用户的最新动态
 * 支持多种格式
 *
 * @author StarWishsama
 */
object DynamicApi : ApiExecutor {
    private const val apiRateLimit = "BiliBili API调用已达上限"

    private val cacheDynamic = mutableMapOf<Long, FeedCardNode>()

    @Throws(ApiException::class)
    suspend fun getDynamicById(id: Long): FeedCardNode? {
        val cache = getCacheDynamic(id)

        if (cache != null) {
            return cache
        }

        if (isReachLimit()) {
            throw RateLimitException(apiRateLimit)
        }

        val feedResp = client.getFeedContent(id.toULong())

        if (feedResp.code != GeneralCode.SUCCESS || feedResp.data == null || feedResp.data?.card == null) {
            throw ApiException("获取动态失败")
        }

        val card = feedResp.data!!.card

        return if (card == null) {
            null
        } else {
            cacheDynamic[id] = card
            card
        }
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    @Throws(ApiException::class)
    suspend fun getUserDynamicTimeline(mid: Int): List<FeedCardNode>? {
        if (isReachLimit()) {
            throw RateLimitException(apiRateLimit)
        }

        try {
            val resp = client.getFeedByUid(0, mid)

            if (resp.code != GeneralCode.SUCCESS || resp.data == null || resp.data?.cards == null) {
                daemonLogger.warning("解析动态时出现异常")
                return null
            }

            resp.data?.cards?.forEach {
                cacheDynamic[(it.description?.dynamicId?.toLong() ?: 0)] = it
            }

            return resp.data?.cards
        } catch (e: Exception) {
            if (e is SerializationException) {
                FileUtil.createErrorReportFile("解析动态失败", "bilibili", e, "", "获取用户动态时间轴")
                throw ApiException("解析动态失败, 无法解析传入 Json")
            } else {
                daemonLogger.warning("解析动态时出现异常", e)
            }

            return null
        }
    }

    fun getCacheDynamic(id: Long): FeedCardNode? = cacheDynamic[id]

    override var usedTime: Int = 0
    override val duration: Int = 3

    override fun isReachLimit(): Boolean {
        val result = usedTime > getLimitTime()
        if (!result) usedTime++
        return result
    }

    override fun getLimitTime(): Int = 10000
}