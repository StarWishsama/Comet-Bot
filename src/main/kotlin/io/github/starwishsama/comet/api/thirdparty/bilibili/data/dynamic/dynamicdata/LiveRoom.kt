package io.github.starwishsama.comet.api.thirdparty.bilibili.data.dynamic.dynamicdata

import com.google.gson.annotations.SerializedName
import io.github.starwishsama.comet.api.thirdparty.bilibili.BiliBiliMainApi
import io.github.starwishsama.comet.api.thirdparty.bilibili.data.dynamic.DynamicData
import io.github.starwishsama.comet.objects.wrapper.MessageWrapper
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class LiveRoom(
        /** 直播间 ID, 例如 21396545 */
        @SerializedName("roomid")
        val roomID: Long,
        /** 用户 UID, 例如 407106379 */
        @SerializedName("uid")
        val uid: Long,
        /** 用户名, 例如 绯赤艾莉欧Official*/
        @SerializedName("uname")
        val userName: String,
        /** 认证类型(?) */
        @SerializedName("verify")
        val verifyName: String?,
        /** 似乎是新加的, 判断是不是 VTuber/Vup, 1为VTuber/Vup */
        @SerializedName("virtual")
        val isVirtual: Int,
        /** 直播间封面图片 */
        @SerializedName("cover")
        val coverImg: String,
        /** 直播时间, 格式 yyyy-MM-dd HH:mm:ss, 例如 2020-10-31 11:02:26 */
        @SerializedName("live_time")
        val liveTime: String,
        @SerializedName("round_status")
        val roundStatus: Int,
        @SerializedName("on_flag")
        val onFlag: Int,
        @SerializedName("title")
        val title: String?,
        /**
         * 直播间 Tag, 例如 "润羽露西娅,潤羽るしあ,VTuber,hololive,虚拟UP主"
         */
        @SerializedName("tags")
        val tags: String,
        /** 直播间封禁时间, 格式 yyyy-MM-dd HH:mm:ss */
        @SerializedName("lock_status")
        val lockStatus: String,
        @SerializedName("hidden_status")
        val hiddenStatus: String,
        @SerializedName("user_cover")
        val userCoverImg: String,
        /** 直播间短ID, 部分UP主才有 */
        @SerializedName("short_id")
        val shortId: Int,
        /** 人气, 肯定不是真实在线人数 */
        @SerializedName("online")
        val onlineCount: Int,
        /** 旧版分区 ID */
        @SerializedName("area")
        val area: Int,
        /** 旧版分区 ID */
        @SerializedName("area_v2_id")
        val areaNew: Int,
        /** 旧版大区 ID */
        @SerializedName("area_v2_parent_id")
        val areaParentNew: Int,
        /** 未知, 可能是警告次数? */
        @SerializedName("attentions")
        val attentionCount: Int,
        /** 直播间背景 */
        @SerializedName("background")
        val liveBackgroundImg: String,
        @SerializedName("room_silent")
        val roomSilent: Int,
        @SerializedName("room_shield")
        val roomShield: Int,
        /** 格式 yyyy-MM-dd HH:mm:ss */
        @SerializedName("try_time")
        val tryTime: String,
        /** 直播分区名, 例如 虚拟主播 */
        @SerializedName("area_v2_name")
        val areaNameNew: String,
        /** 首次直播时间, 时间戳 */
        @SerializedName("first_live_time")
        val firstLiveTime: Long,
        @SerializedName("live_status")
        val liveStatus: Int,
        @SerializedName("area_v2_parent_name")
        val areaParentNameNew: String,
        @SerializedName("broadcast_type")
        val broadcastType: Int,
        @SerializedName("face")
        val face: String
) : DynamicData {
    val yyMMddPattern: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    private fun getRoomURL(): String = "https://live.bilibili.com/$roomID"

    enum class Status(var status: String) {
        NoStreaming("闲置"), Streaming("直播中"), PlayingVideo("轮播中"), Unknown("未知");
    }

    private fun getStatus(roundStatus: Int): Status {
        return when (roundStatus) {
            0 -> Status.NoStreaming
            1 -> Status.Streaming
            2 -> Status.PlayingVideo
            else -> Status.Unknown
        }
    }

    override suspend fun getContact(): MessageWrapper {
        val wrapped = MessageWrapper(
                "${BiliBiliMainApi.getUserNameByMid(uid)}的直播间\n" +
                        "直播间标题: ${title}\n" +
                        "直播状态: ${getStatus(roundStatus).status}\n" +
                        "直达链接: ${getRoomURL()}\n"
        )
        if (coverImg.isNotEmpty()) {
            wrapped.plusImageUrl(coverImg)
        }
        return wrapped
    }

    override fun getSentTime(): LocalDateTime = LocalDateTime.from(yyMMddPattern.parse(liveTime))
}