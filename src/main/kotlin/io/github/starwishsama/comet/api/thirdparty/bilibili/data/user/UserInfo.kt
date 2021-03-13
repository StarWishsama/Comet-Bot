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
): CommonResponse() {
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