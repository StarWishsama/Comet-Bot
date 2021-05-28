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
 * 端点: http://api.bilibili.com/x/space/acc/info
 */
data class UserInfo(
    val data: Data
) : CommonResponse() {
    data class Data(
        @JsonProperty("mid")
        val memberId: Long,
        @JsonProperty("name")
        val userName: String,
        @JsonProperty("sex")
        val sex: String,
        @JsonProperty("face")
        val avatar: String,
        @JsonProperty("sign")
        val sign: String,
        @JsonProperty("rank")
        val rank: Long,
        @JsonProperty("level")
        val level: Int,
        @JsonProperty("official")
        val officialInfo: OfficialInfo?,
        @JsonProperty("vip")
        val vipInfo: VipInfo,
        @JsonProperty("pendant")
        val pendant: Pendant,
        @JsonProperty("live_room")
        val liveRoomInfo: LiveRoomInfo
    ) {
        data class OfficialInfo(
            @JsonProperty("role")
            val role: Int,
            @JsonProperty("title")
            val title: String,
            @JsonProperty("desc")
            val desc: String,
            @JsonProperty("type")
            val type: Int
        )

        data class VipInfo(
            val type: Int,
            val status: Int
        )

        data class Pendant(
            val pid: Int,
            val name: String,
            val image: String,
            val expire: Int,
            @JsonProperty("image_enhance")
            val imageEnhance: String
        )

        data class LiveRoomInfo(
            val roomStatus: Int,
            val liveStatus: Int,
            @JsonProperty("roomid")
            val roomId: Long
        )
    }
}