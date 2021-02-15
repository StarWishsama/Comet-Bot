package io.github.starwishsama.comet.api.thirdparty.bilibili.data.user

import com.google.gson.annotations.SerializedName
import io.github.starwishsama.comet.api.thirdparty.bilibili.data.CommonResponse

/**
 * 通过 UID 查询到的用户信息
 *
 * 端点: http://api.bilibili.com/x/space/acc/info
 */
data class UserInfo(
    @Transient
    override val code: Int,
    @Transient
    override val message: String,
    @Transient
    override val ttl: Int,
    val data: Data
): CommonResponse(code, message, ttl) {
    data class Data(
        @SerializedName("mid")
        val memberId: Long,
        @SerializedName("name")
        val userName: String,
        @SerializedName("sex")
        val sex: String,
        @SerializedName("face")
        val avatar: Long,
        @SerializedName("sign")
        val sign: String,
        @SerializedName("rank")
        val rank: Long,
        @SerializedName("level")
        val level: Int,
        @SerializedName("official")
        val officialInfo: OfficialInfo?,
        @SerializedName("vip")
        val vipInfo: VipInfo,
        @SerializedName("pendant")
        val pendant: Pendant,
        @SerializedName("live_room")
        val liveRoomInfo: LiveRoomInfo
    ) {
        data class OfficialInfo(
            @SerializedName("role")
            val role: Int,
            @SerializedName("title")
            val title: String,
            @SerializedName("desc")
            val desc: String,
            @SerializedName("type")
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
            @SerializedName("image_enhance")
            val imageEnhance: String
        )

        data class LiveRoomInfo(
            val roomStatus: Int,
            val liveStatus: Int,
            @SerializedName("roomid")
            val roomId: Long
        )
    }
}