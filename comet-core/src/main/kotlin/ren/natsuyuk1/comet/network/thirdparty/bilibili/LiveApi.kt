/*
 * Copyright (c) 2019-2022 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package ren.natsuyuk1.comet.network.thirdparty.bilibili

import moe.sdl.yabapi.api.getRoomInfoByRoomId

object LiveApi {
    private const val apiRateLimit = "BiliBili API调用已达上限"

    suspend fun getLiveInfo(roomId: Int) = runCatching {
        if (roomId < 1L) {
            throw IllegalArgumentException("直播间 ID 不能小于 1")
        }

        client.getRoomInfoByRoomId(roomId).data
    }
}
