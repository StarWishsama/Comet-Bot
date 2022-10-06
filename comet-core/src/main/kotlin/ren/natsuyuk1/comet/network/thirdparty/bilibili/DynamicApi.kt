package ren.natsuyuk1.comet.network.thirdparty.bilibili

import kotlinx.serialization.SerializationException
import moe.sdl.yabapi.api.getFeedByUid
import moe.sdl.yabapi.api.getFeedContent
import moe.sdl.yabapi.data.GeneralCode
import moe.sdl.yabapi.data.feed.FeedCardNode
import mu.KotlinLogging
import ren.natsuyuk1.comet.network.exception.ApiException

private val logger = KotlinLogging.logger {}

/**
 * BiliBili 动态 API
 *
 * 获取用户的最新动态
 * 支持多种格式
 *
 * @author StarWishsama
 */
object DynamicApi {
    private val cacheDynamic = mutableMapOf<Long, FeedCardNode>()

    suspend fun getDynamic(id: Long) = runCatching {
        val cache = getCacheDynamic(id)

        if (cache != null) {
            return@runCatching cache
        }

        val feedResp = biliClient.getFeedContent(id.toULong())

        if (feedResp.code != GeneralCode.SUCCESS || feedResp.data == null || feedResp.data?.card == null) {
            throw ApiException("获取动态失败, 状态码 ${feedResp.code.name}")
        }

        val card = feedResp.data!!.card

        if (card == null) {
            null
        } else {
            cacheDynamic[id] = card
            card
        }
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    @Throws(ApiException::class)
    suspend fun getUserDynamicTimeline(mid: Long): List<FeedCardNode>? {
        try {
            val resp = biliClient.getFeedByUid(0, mid)

            if (resp.code != GeneralCode.SUCCESS || resp.data == null || resp.data?.cards == null) {
                logger.warn { "解析动态时出现异常" }
                logger.warn { "原始请求: $resp" }
                return null
            }

            resp.data?.cards?.forEach {
                cacheDynamic[(it.description?.dynamicId?.toLong() ?: 0)] = it
            }

            return resp.data?.cards
        } catch (e: Exception) {
            if (e is SerializationException) {
                throw ApiException("解析动态失败, 无法解析传入 Json", e)
            } else {
                logger.warn(e) { "解析动态时出现异常" }
            }

            return null
        }
    }

    private fun getCacheDynamic(id: Long): FeedCardNode? = cacheDynamic[id]
}
