package io.github.starwishsama.comet.api.thirdparty.bilibili.live

import moe.sdl.yabapi.data.live.LiveRoomData
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

fun LiveRoomData.isLiveTimeInvalid(): Boolean {
    if (liveTime == null || liveTime?.isEmpty() == true) {
        return true
    }

    val timepart = liveTime!!.split("-")
    return liveTime == "0000-00-00 00:00:00" || timepart[0].toLong() !in -999999999..999999999
}

enum class LiveStatus(var status: String) {
    NoStreaming("闲置"), Streaming("直播"), PlayingVideo("轮播"), Unknown("未知");
}

fun LiveRoomData.getStatus(): LiveStatus {
    return when (liveStatus) {
        0 -> LiveStatus.NoStreaming
        1 -> LiveStatus.Streaming
        2 -> LiveStatus.PlayingVideo
        else -> LiveStatus.Unknown
    }
}

fun LiveRoomData.parseLiveTime(): LocalDateTime {
    return if (isLiveTimeInvalid()) {
        LocalDateTime.MIN
    } else {
        LocalDateTime.parse(liveTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
    }
}