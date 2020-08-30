package io.github.starwishsama.bilibiliapi.data.dynamic.dynamicdata

import com.google.gson.annotations.SerializedName
import io.github.starwishsama.bilibiliapi.data.dynamic.DynamicData
import io.github.starwishsama.bilibiliapi.data.live.LiveRoomInfo
import io.github.starwishsama.comet.objects.MessageWrapper

data class LiveRoom(@SerializedName("round_status")
                    val roundStatus: Int,
                    @SerializedName("roomid")
                    val roomID: Long,
                    @SerializedName("room_info")
                    val roomInfo: LiveRoomInfo.LiveRoomInfoData
) : DynamicData {
    override suspend fun getContact(): MessageWrapper {
        val wrapped = MessageWrapper("的直播间\n" +
                "直播间标题:${roomInfo.title}\n" +
                "直播状态: ${roomInfo.getStatus(roundStatus).status}\n" +
                "直达链接: ${roomInfo.getRoomURL()}\n")
        if (roomInfo.backgroundImageUrl.isNotEmpty()) {
            wrapped.picUrl = roomInfo.backgroundImageUrl
        }
        return wrapped
    }
}