/*
 * Copyright (c) 2019-2022 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package io.github.starwishsama.comet.api.thirdparty.bilibili

import io.github.starwishsama.comet.api.thirdparty.ApiExecutor
import io.github.starwishsama.comet.exceptions.RateLimitException
import io.github.starwishsama.comet.utils.FileUtil
import moe.sdl.yabapi.api.getRoomInfoByRoomId
import moe.sdl.yabapi.data.live.LiveRoomData
import moe.sdl.yabapi.data.live.LiveRoomInfoResponse

object LiveApi : ApiExecutor {
    private const val apiRateLimit = "BiliBili API调用已达上限"

    @Throws(RateLimitException::class)
    suspend fun getLiveInfo(roomId: Int): LiveRoomData? {
        if (isReachLimit()) {
            throw RateLimitException(apiRateLimit)
        }

        if (roomId < 1L) {
            throw IllegalArgumentException("直播间 ID 不能小于 1")
        }

        var resp: LiveRoomInfoResponse? = null

        return kotlin.runCatching {
            resp = client.getRoomInfoByRoomId(roomId)
            resp?.data
        }.onFailure {
            FileUtil.createErrorReportFile("在获取B站直播间信息时出现了意外", "bilibili", it, "LiveApi#getLiveInfo() roomId = $roomId", "${resp?.code}\n${resp?.message}")
        }.getOrNull()
    }

    override fun isReachLimit(): Boolean {
        val result = DynamicApi.usedTime > DynamicApi.getLimitTime()
        if (!result) DynamicApi.usedTime++
        return result
    }

    override var usedTime: Int = 0
    override val duration: Int = 3

    override fun getLimitTime(): Int = 15000
}