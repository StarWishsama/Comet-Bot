package io.github.starwishsama.bilibiliapi.data.liveroom

import com.google.gson.annotations.SerializedName

data class RoomInfo(
    @SerializedName("area_name")
    val subArea: String,
    @SerializedName("parent_area_name")
    val parentArea: String,
    @SerializedName("cover")
    val coverURL: String,
    @SerializedName("room_id")
    var roomID: Long = 0,
    @SerializedName("live_start_time")
    var startTime: Long = 0,
    @SerializedName("live_status")
    private val status: Short = 0,
    @SerializedName("keyframe")
    val keyframeURL: String,
    @SerializedName("title")
    val title: String){

    fun getRoomURL(): String {
        return "https://live.data.com/$roomID"
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