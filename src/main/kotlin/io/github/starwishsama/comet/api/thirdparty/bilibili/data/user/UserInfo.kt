/*
 * Copyright (c) 2019-2021 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package io.github.starwishsama.comet.api.thirdparty.bilibili.data.user

import com.fasterxml.jackson.annotation.JsonProperty
import io.github.starwishsama.comet.api.thirdparty.bilibili.data.CommonResponse

/**
 * 通过 UID 查询到的用户信息
 *
 * 端点: http://api.bilibili.com/x/web-interface/card
 */
data class UserInfo(
    val data: Data
) : CommonResponse() {
    data class Data(
        val card: InfoCard,
        val follower: Long
    ) {
        data class InfoCard(
            val mid: Long,
            val name: String,
            val sex: String,
            val face: String,
            @JsonProperty("level_info")
            val levelInfo: LevelInfo,
        ) {
            data class LevelInfo(
                @JsonProperty("current_level")
                val currentLevel: Int,
            )
        }
    }
}