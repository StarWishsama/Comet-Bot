/*
 * Copyright (c) 2019-2022 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package ren.natsuyuk1.comet.api.thirdparty.bilibili

import cn.hutool.http.HttpRequest
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.module.kotlin.readValue
import io.github.starwishsama.comet.CometVariables.mapper
import io.github.starwishsama.comet.api.thirdparty.ApiExecutor
import io.github.starwishsama.comet.api.thirdparty.bilibili.data.live.LiveRoomInfo
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

        if (roomId < 1L) {
            throw IllegalArgumentException("直播间 ID 不能小于 1")
        }

        val request = HttpRequest.get(liveUrl + roomId).timeout(2000)
            .header("User-Agent", "Bili live status checker")
        val response = request.executeAsync()

        return try {
            mapper.readValue(response.body())
        } catch (t: Throwable) {
            FileUtil.createErrorReportFile("在获取B站直播间信息时出现了意外", "bilibili", t, response.body(), request.url)
            null
        }
    }

    override fun isReachLimit(): Boolean {
        val result = DynamicApi.usedTime > DynamicApi.getLimitTime()
        if (!result) DynamicApi.usedTime++
        return result
    }

    override var usedTime: Int = 0
    override val duration: Int = 3

    override fun getLimitTime(): Int = 15000

    private data class OldRoomInfo(
        val code: Int,
        val message: String,
        val data: LiveInfoData
    ) {
        data class LiveInfoData(
            @JsonProperty("roomid")
            val roomId: Long
        )
    }
}