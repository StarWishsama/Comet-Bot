/*
 * Copyright (c) 2019-2021 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package ren.natsuyuk1.comet.network.thirdparty.bilibili

import moe.sdl.yabapi.api.getVideoInfo
import moe.sdl.yabapi.data.video.VideoInfo
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

object VideoApi {
    suspend fun getVideoInfo(av: Int): VideoInfo? {
        return kotlin.runCatching { client.getVideoInfo(av).data }.onFailure {
            logger.warn("在获取哔哩哔哩视频信息时遇到了问题", it)
        }.getOrNull()
    }

    suspend fun getVideoInfo(bv: String): Result<VideoInfo?> {
        return kotlin.runCatching { client.getVideoInfo(bv).data }.onFailure {
            logger.warn("在获取哔哩哔哩视频信息时遇到了问题", it)
        }
    }
}
