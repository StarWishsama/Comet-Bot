package io.github.starwishsama.comet.api.bilibili.data.dynamic.dynamicdata

import com.google.gson.annotations.SerializedName
import io.github.starwishsama.comet.api.bilibili.data.dynamic.DynamicData
import io.github.starwishsama.comet.api.bilibili.data.liveroom.LiveInfo
import io.github.starwishsama.comet.objects.MessageWrapper

data class LiveRoom(@SerializedName("round_status")
                    val roundStatus: Int,
                    @SerializedName("roomid")
                    val roomID: Long) : LiveInfo(), DynamicData {
    override suspend fun getContact(): MessageWrapper {
        val wrapped = MessageWrapper("的直播间\n" +
                "直播间标题:${roomInfo.title}\n" +
                "直播状态: ${roomInfo.getStatus(roundStatus).status}\n" +
                "直达链接: ${roomInfo.getRoomURL()}\n")
        if (roomInfo.coverURL.isNotEmpty()) {
            wrapped.picUrl = roomInfo.coverURL
        }
        return wrapped
    }
}