package io.github.starwishsama.bilibiliapi

import cn.hutool.http.HttpRequest
import com.github.salomonbrys.kotson.fromJson
import io.github.starwishsama.bilibiliapi.data.live.LiveRoomInfo
import io.github.starwishsama.comet.BotVariables.gson
import io.github.starwishsama.comet.api.ApiExecutor
import io.github.starwishsama.comet.exceptions.RateLimitException
import io.github.starwishsama.comet.utils.FileUtil

object LiveApi : ApiExecutor {
    private const val liveUrl = "http://api.live.bilibili.com/room/v1/Room/get_info?id="
    private const val apiRateLimit = "BiliBili API调用已达上限"

    @Throws(RateLimitException::class)
    fun getLiveInfo(roomId: Long): LiveRoomInfo? {
        if (isReachLimit()) {
            throw RateLimitException(apiRateLimit)
        }

        usedTime++
        val request = HttpRequest.get(liveUrl + roomId).timeout(500)
                .header("User-Agent", "Bili live status checker by StarWishsama")
        val response = request.executeAsync()

        return try {
            gson.fromJson(response.body())
        } catch (t: Throwable) {
            FileUtil.createErrorReportFile("在获取B站直播间信息时出现了意外", "bilibili", t, response.body(), request.url)
            null
        }
    }

    @Throws(RateLimitException::class)
    fun getLiveStatus(mid: Long): Boolean {
        val info = getLiveInfo(mid)
        return info?.data?.liveStatus == 1
    }

    @Throws(RateLimitException::class)
    fun getRoomIdByMid(mid: Long): Long {
        val info = getLiveInfo(mid)
        return info?.data?.roomId ?: -1
    }

    override var usedTime: Int = 0

    override fun getLimitTime(): Int = 1500
}