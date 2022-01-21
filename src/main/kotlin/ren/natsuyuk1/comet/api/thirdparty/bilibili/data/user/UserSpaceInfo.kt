/*
 * Copyright (c) 2019-2022 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package ren.natsuyuk1.comet.api.thirdparty.bilibili.data.user

import com.fasterxml.jackson.annotation.JsonProperty
import io.github.starwishsama.comet.api.thirdparty.bilibili.data.CommonResponse

data class UserSpaceInfo(
    val data: Data
) : CommonResponse() {
    data class Data(
        val mid: Long,
        val name: String,
        val sex: String,
        val face: String,
        val sign: String,
        @JsonProperty("live_room")
        val liveRoomInfo: LiveRoomInfo
    ) {
        data class LiveRoomInfo(
            val roomStatus: Int,
            val liveStatus: Int,
            val url: String,
            val online: Long,
            @JsonProperty("roomid")
            val roomID: Long,
        )
    }
}
