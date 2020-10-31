package io.github.starwishsama.comet.api.thirdparty.bilibili.data.live

import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * http://api.live.bilibili.com/room/v1/Room/get_info?id=<param>
 */
data class LiveRoomInfo(
    val code: Int,
    val msg: String?,
    val message: String?,
    val ttl: Int?,
    val data: LiveRoomInfoData
) {
    data class LiveRoomInfoData(
        val uid: Long,
        @SerializedName("room_id")
        val roomId: Long,
        @SerializedName("short_id")
        val shortRoomId: Int,
        @SerializedName("attention")
        val attentionCount: Long,
        @SerializedName("online")
        val onlineCount: Int,
        @SerializedName("is_portrait")
        val portrait: Boolean,
        val description: String?,
        /**
         * 0 未在直播 1 正在直播 2 投稿视频轮播
         */
        @SerializedName("live_status")
        val liveStatus: Int,
        /**
         * 直播分区 ID
         */
        @SerializedName("area_id")
        val areaId: Int,
        /**
         * 直播大区 ID
         */
        @SerializedName("parent_area_id")
        val parentAreaId: Int,
        /**
         * 直播大区名
         */
        @SerializedName("parent_area_name")
        val parentAreaName: String,
        @SerializedName("old_area_id")
        val oldAreaId: Int,
        @SerializedName("background")
        val backgroundImageUrl: String,
        @SerializedName("title")
        val title: String?,
        @SerializedName("user_cover")
        val liveRoomCoverImageUrl: String,
        /**
         * 直播画面的某一帧图片
         */
        @SerializedName("keyframe")
        val keyFrameImageUrl: String,
        @SerializedName("is_strict_room")
        val strictMode: Boolean,
        /**
         * 开播时间 格式 yyyy-MM-dd HH:mm:ss
         */
        @SerializedName("live_time")
        val liveTime: String,
        /**
         * 直播间 Tag, 例如 "润羽露西娅,潤羽るしあ,VTuber,hololive,虚拟UP主"
         */
        val tags: String,
        /**
         * 未知, 锚 -> 上舰?
         */
        @SerializedName("is_anchor")
        val anchor: Int,
        @SerializedName("room_silent_type")
        val roomSilentType: String,
        @SerializedName("room_silent_level")
        val roomSilentLevel: Int,
        @SerializedName("room_silent_second")
        val roomSilentSecond: Int,
        /**
         * 分区名
         */
        @SerializedName("area_name")
        val areaName: String,
        @SerializedName("pendants")
        val pendants: String,
        @SerializedName("area_pendants")
        val areaPendants: String,
        @SerializedName("hot_words")
        val hotWords: List<String>,
        @SerializedName("hot_words_status")
        val hotWordsStatus: Int,
        val verify: String?,
        @SerializedName("new_pendants")
        val newPendants: NewPendants,
        @SerializedName("up_session")
        val upSession: String?,
        @SerializedName("pk_status")
        val pkStatus: Int,
        @SerializedName("pk_id")
        val pkId: Int,
        @SerializedName("battle_id")
        val battleId: Int,
        @SerializedName("allow_change_area_time")
        val changeAreaTime: Int,
        @SerializedName("allow_upload_cover_time")
        val uploadCoverTime: Int,
        @SerializedName("studio_info")
        val studioInfo: JsonElement?
    ) {
        data class NewPendants(
                /**
                 * "badge": {
                 * "name": "v_person",
                 * "position": 3,
                 * "value": "",
                 * "desc": "bilibili直播签约主播"
                 * }
                 */
                @SerializedName("badge")
                var badge: JsonElement?,
                @SerializedName("frame")
                var frame: JsonElement?,
                @SerializedName("mobile_badge")
                var mobileBadge: JsonElement?,
                @SerializedName("mobile_frame")
                var mobileFrame: JsonElement?
        )

        fun isLiveNow(): Boolean = liveStatus == 1

        fun getLiveTime(): LocalDateTime {
            return LocalDateTime.parse(liveTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        }

        fun getRoomURL(): String {
            return "https://live.bilibili.com/$roomId"
        }

        enum class Status(var status: String) {
            NoStreaming("闲置"), Streaming("直播"), PlayingVideo("轮播"), Unknown("未知");
        }


        fun getStatus(roundStatus: Int): Status {
            return when (roundStatus) {
                0 -> Status.NoStreaming
                1 -> Status.Streaming
                2 -> Status.PlayingVideo
                else -> Status.Unknown
            }
        }
    }
}