package io.github.starwishsama.comet.api.thirdparty.bilibili.data.live

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.JsonNode
import io.github.starwishsama.comet.api.thirdparty.bilibili.data.CommonResponse
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * http://api.live.bilibili.com/room/v1/Room/get_info?id=<param>
 */
data class LiveRoomInfo(
    val data: LiveRoomInfoData
) : CommonResponse() {
    data class LiveRoomInfoData(
        val uid: Long,
        @JsonProperty("room_id")
        val roomId: Long,
        @JsonProperty("short_id")
        val shortRoomId: Int,
        @JsonProperty("attention")
        val attentionCount: Long,
        @JsonProperty("online")
        val onlineCount: Int,
        @JsonProperty("is_portrait")
        val portrait: Boolean,
        val description: String?,
        /**
         * 0 未在直播 1 正在直播 2 投稿视频轮播
         */
        @JsonProperty("live_status")
        val liveStatus: Int,
        /**
         * 直播分区 ID
         */
        @JsonProperty("area_id")
        val areaId: Int,
        /**
         * 直播大区 ID
         */
        @JsonProperty("parent_area_id")
        val parentAreaId: Int,
        /**
         * 直播大区名
         */
        @JsonProperty("parent_area_name")
        val parentAreaName: String,
        @JsonProperty("old_area_id")
        val oldAreaId: Int,
        @JsonProperty("background")
        val backgroundImageUrl: String,
        @JsonProperty("title")
        val title: String?,
        @JsonProperty("user_cover")
        val liveRoomCoverImageUrl: String,
        /**
         * 直播画面的某一帧图片
         */
        @JsonProperty("keyframe")
        val keyFrameImageUrl: String,
        @JsonProperty("is_strict_room")
        val strictMode: Boolean,
        /**
         * 开播时间 格式 yyyy-MM-dd HH:mm:ss
         */
        @JsonProperty("live_time")
        var liveTime: String,
        /**
         * 直播间 Tag
         */
        val tags: String,
        /**
         * 未知, 锚 -> 上舰?
         */
        @JsonProperty("is_anchor")
        val anchor: Int,
        @JsonProperty("room_silent_type")
        val roomSilentType: String,
        @JsonProperty("room_silent_level")
        val roomSilentLevel: Int,
        @JsonProperty("room_silent_second")
        val roomSilentSecond: Int,
        /**
         * 分区名
         */
        @JsonProperty("area_name")
        val areaName: String,
        @JsonProperty("pendants")
        val pendants: String,
        @JsonProperty("area_pendants")
        val areaPendants: String,
        val verify: String?,
        @JsonProperty("new_pendants")
        val newPendants: NewPendants,
        @JsonProperty("up_session")
        val upSession: String?,
        @JsonProperty("allow_change_area_time")
        val changeAreaTime: Int,
        @JsonProperty("allow_upload_cover_time")
        val uploadCoverTime: Int,
    ) {
        init {
            if (isEmptyTime()) {
                liveTime = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.MIN)
            }
        }

        data class NewPendants(
            /**
             * "badge": {
             * "name": "v_person",
             * "position": 3,
             * "value": "",
             * "desc": "bilibili直播签约主播"
             * }
             */
            @JsonProperty("badge")
            var badge: JsonNode?,
            @JsonProperty("frame")
            var frame: JsonNode?,
            @JsonProperty("mobile_badge")
            var mobileBadge: JsonNode?,
            @JsonProperty("mobile_frame")
            var mobileFrame: JsonNode?
        )

        fun isLiveNow(): Boolean = getStatus() == Status.Streaming

        fun parseLiveTime(): LocalDateTime {
            return if (isEmptyTime()) {
                LocalDateTime.MIN
            } else {
                LocalDateTime.parse(liveTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            }
        }

        fun isEmptyTime(): Boolean {
            return liveTime == "0000-00-00 00:00:00"
        }

        fun getRoomURL(): String {
            return "https://live.bilibili.com/$roomId"
        }

        enum class Status(var status: String) {
            NoStreaming("闲置"), Streaming("直播"), PlayingVideo("轮播"), Unknown("未知");
        }


        fun getStatus(): Status {
            return when (liveStatus) {
                0 -> Status.NoStreaming
                1 -> Status.Streaming
                2 -> Status.PlayingVideo
                else -> Status.Unknown
            }
        }
    }
}