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

import io.github.starwishsama.comet.CometVariables
import io.github.starwishsama.comet.api.thirdparty.ApiExecutor
import moe.sdl.yabapi.api.getVideoInfo
import moe.sdl.yabapi.data.video.VideoInfo

object VideoApi : ApiExecutor {
    suspend fun getVideoInfo(aid: Int): VideoInfo? {
        return kotlin.runCatching { client.getVideoInfo(aid).data }.onFailure {
            CometVariables.daemonLogger.warning("在获取哔哩哔哩视频信息时遇到了问题", it)
        }.getOrNull()
    }

    suspend fun getVideoInfo(bvID: String): VideoInfo? {
        return kotlin.runCatching { client.getVideoInfo(bvID).data }.onFailure {
            CometVariables.daemonLogger.warning("在获取哔哩哔哩视频信息时遇到了问题", it)
        }.getOrNull()
    }

    override var usedTime: Int = 0
    override val duration: Int = 3

    override fun getLimitTime(): Int = 3500
}