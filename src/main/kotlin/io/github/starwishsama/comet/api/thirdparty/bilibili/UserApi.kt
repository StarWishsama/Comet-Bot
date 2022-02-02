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

import io.github.starwishsama.comet.api.thirdparty.ApiExecutor
import io.github.starwishsama.comet.exceptions.RateLimitException
import kotlinx.coroutines.runBlocking
import moe.sdl.yabapi.api.getUserCard
import moe.sdl.yabapi.api.getUserSpace
import moe.sdl.yabapi.data.info.UserCardGetData
import moe.sdl.yabapi.data.info.UserSpace

object UserApi : ApiExecutor {
    suspend fun getUserCard(id: Int): UserCardGetData? {
        return client.getUserCard(id, false).data
    }

    suspend fun getUserSpace(id: Int): UserSpace? {
        return client.getUserSpace(id).data
    }

    @Throws(RateLimitException::class)
    fun getUserNameByMid(mid: Int): String {
        return runBlocking { return@runBlocking getUserCard(mid)?.card?.name!! }
    }

    override var usedTime: Int = 0
    override val duration: Int = 3

    override fun getLimitTime(): Int = 3500
}