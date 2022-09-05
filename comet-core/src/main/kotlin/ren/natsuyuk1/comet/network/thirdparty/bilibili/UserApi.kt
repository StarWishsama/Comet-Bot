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

import moe.sdl.yabapi.api.getUserCard
import moe.sdl.yabapi.api.getUserSpace
import moe.sdl.yabapi.data.info.UserCardGetData
import moe.sdl.yabapi.data.info.UserSpace

object UserApi {
    suspend fun getUserCard(id: Int): UserCardGetData? {
        return biliClient.getUserCard(id, false).data
    }

    suspend fun getUserSpace(id: Int): UserSpace? {
        return biliClient.getUserSpace(id).data
    }

    suspend fun getUserNameByMid(mid: Int): String {
        return getUserCard(mid)?.card?.name!!
    }
}
