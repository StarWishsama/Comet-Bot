package io.github.starwishsama.nbot.objects.bilibili.dynamic.dynamicdata

import cn.hutool.http.HttpRequest
import com.google.gson.annotations.SerializedName
import io.github.starwishsama.nbot.objects.bilibili.dynamic.DynamicData
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.asMessageChain
import net.mamoe.mirai.message.data.toMessage
import net.mamoe.mirai.message.uploadAsImage

data class Video(var dynamic: String,
                 var aid: Long,
                 @SerializedName("pic")
                 var picURL: String?,
                 var title: String) : DynamicData {
    override suspend fun getMessageChain(contact: Contact): MessageChain {
        val text = ("发布了一个视频: $title\n直达链接: https://www.bilibili.com/video/$aid").toMessage().asMessageChain()

        if (picURL != null){
            val response = HttpRequest.get(picURL).timeout(150_000).executeAsync().bodyStream()
            return text.plus(response.uploadAsImage(contact))
        }
        return text
    }

}