package ren.natsuyuk1.comet.network.thirdparty.bilibili.live

import kotlinx.serialization.Serializable
import moe.sdl.yabapi.data.live.LiveRoomData
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

fun String.isLiveTimeInvalid(): Boolean {
    if (isEmpty()) {
        return true
    }

    val timepart = split("-")
    return this == "0000-00-00 00:00:00" || timepart[0].toLong() !in -999999999..999999999
}

fun LiveRoomData.isLiveTimeInvalid(): Boolean {
    if (liveTime == null) {
        return true
    }

    return liveTime!!.isLiveTimeInvalid()
}

enum class LiveStatus(var status: String) {
    NoStreaming("闲置"), Streaming("直播"), PlayingVideo("轮播"), Unknown("未知");
}

fun LiveRoomData.toSimpleLiveRoomData(): SimpleLiveRoomData =
    SimpleLiveRoomData(roomId, title, liveTime, getStatus(), userCover)

fun LiveRoomData.getStatus(): LiveStatus {
    return when (liveStatus) {
        0 -> LiveStatus.NoStreaming
        1 -> LiveStatus.Streaming
        2 -> LiveStatus.PlayingVideo
        else -> LiveStatus.Unknown
    }
}

fun String.parseLiveTime(): LocalDateTime {
    return if (isLiveTimeInvalid()) {
        LocalDateTime.MIN
    } else {
        LocalDateTime.parse(this, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
    }
}

fun LiveRoomData.parseLiveTime(): LocalDateTime {
    return liveTime?.parseLiveTime() ?: LocalDateTime.MIN
}

@Serializable
data class SimpleLiveRoomData(
    val roomId: Long,
    val roomTitle: String?,
    val liveTime: String?,
    val liveStatus: LiveStatus,
    val cover: String?,
)
