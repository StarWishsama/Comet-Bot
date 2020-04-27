package io.github.starwishsama.nbot.objects.bilibili.dynamic.dynamicdata

import cn.hutool.http.HttpRequest
import com.google.gson.annotations.SerializedName
import io.github.starwishsama.nbot.objects.bilibili.dynamic.DynamicData
import io.github.starwishsama.nbot.objects.bilibili.liveroom.LiveInfo
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.asMessageChain
import net.mamoe.mirai.message.data.toMessage
import net.mamoe.mirai.message.uploadAsImage

data class LiveRoom(@SerializedName("round_status")
                    val roundStatus: Int,
                    @SerializedName("roomid")
                    val roomID: Long) : LiveInfo(), DynamicData {
    override suspend fun getMessageChain(contact: Contact): MessageChain {
        val text = "的直播间\n直播间标题:${roomInfo.title}\n直播状态: ${roomInfo.getStatus(roundStatus).status}\n直达链接: https://live.bilibili.com/${roomInfo.roomID}".toMessage().asMessageChain()
        if (roomInfo.coverURL.isNotEmpty()){
            val response = HttpRequest.get(roomInfo.coverURL).timeout(150_000).executeAsync().bodyStream()
            return text.plus(response.uploadAsImage(contact))
        }
        return text
    }
}