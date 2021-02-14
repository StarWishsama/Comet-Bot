package io.github.starwishsama.comet.api.thirdparty.bilibili

import cn.hutool.http.HttpRequest
import com.github.salomonbrys.kotson.fromJson
import com.google.gson.annotations.SerializedName
import io.github.starwishsama.comet.BotVariables.nullableGson
import io.github.starwishsama.comet.api.thirdparty.ApiExecutor
import io.github.starwishsama.comet.api.thirdparty.bilibili.data.live.LiveRoomInfo
import io.github.starwishsama.comet.exceptions.RateLimitException
import io.github.starwishsama.comet.utils.FileUtil
import io.github.starwishsama.comet.utils.network.NetUtil

object LiveApi : ApiExecutor {
    private const val liveUrl = "http://api.live.bilibili.com/room/v1/Room/get_info?id="
    private const val liveOldUrl = "https://api.live.bilibili.com/room/v1/Room/getRoomInfoOld?mid="
    private const val apiRateLimit = "BiliBili API调用已达上限"

    @Throws(RateLimitException::class)
    fun getLiveInfo(roomId: Long): LiveRoomInfo? {
        if (isReachLimit()) {
            throw RateLimitException(apiRateLimit)
        }

        val request = HttpRequest.get(liveUrl + roomId).timeout(500)
            .header("User-Agent", "Bili live status checker by StarWishsama")
        val response = request.executeAsync()

        return try {
            nullableGson.fromJson(response.body())
        } catch (t: Throwable) {
            FileUtil.createErrorReportFile("在获取B站直播间信息时出现了意外", "bilibili", t, response.body(), request.url)
            null
        }
    }

    fun getRoomIDByUID(uid: Long): Long {
        val result = NetUtil.executeHttpRequest(
                url = (liveOldUrl + uid)
        )

        result.use {
            return try {
                val bodyString = result.body?.string()
                println(bodyString)
                val info = nullableGson.fromJson<OldRoomInfo>(bodyString ?: return -1)
                info.data.roomId
            } catch (e: Exception) {
                -1
            }
        }
    }

    override fun isReachLimit(): Boolean {
        val result = BiliBiliMainApi.usedTime > BiliBiliMainApi.getLimitTime()
        if (!result) BiliBiliMainApi.usedTime++
        return result
    }

    override var usedTime: Int = 0
    override val duration: Int = 3

    override fun getLimitTime(): Int = 1500

    private data class OldRoomInfo(
        val data: LiveInfoData
    ) {
        data class LiveInfoData(
            @SerializedName("roomid")
            val roomId: Long
        )
    }
}