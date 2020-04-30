package io.github.starwishsama.nbot.objects.bilibili.dynamic.dynamicdata

import com.google.gson.annotations.SerializedName
import io.github.starwishsama.nbot.objects.bilibili.dynamic.DynamicData
import io.github.starwishsama.nbot.objects.bilibili.liveroom.LiveInfo

data class LiveRoom(@SerializedName("round_status")
                    val roundStatus: Int,
                    @SerializedName("roomid")
                    val roomID: Long) : LiveInfo(), DynamicData {
    override suspend fun getContact(): List<String> {
        val list = arrayListOf<String>()
        list.add("的直播间\n直播间标题:${roomInfo.title}\n直播状态: ${roomInfo.getStatus(roundStatus).status}\n直达链接: ${roomInfo.getRoomURL()}\n")
        if (roomInfo.coverURL.isNotEmpty()){
            list.add(roomInfo.coverURL)
        }
        return list
    }
}