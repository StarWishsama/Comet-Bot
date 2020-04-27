package io.github.starwishsama.nbot.objects.bilibili.dynamic.dynamicdata

import cn.hutool.http.HttpRequest
import com.google.gson.annotations.SerializedName
import io.github.starwishsama.nbot.objects.bilibili.dynamic.DynamicData
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.asMessageChain
import net.mamoe.mirai.message.data.toMessage
import net.mamoe.mirai.message.uploadAsImage

data class Music(var id: Long,
                 @SerializedName("cover")
                 var coverURL: String?,
                 @SerializedName("intro")
                 var dynamic: String) : DynamicData {
    override suspend fun getMessageChain(contact: Contact): MessageChain {
        val text = "发布了音乐 $dynamic".toMessage().asMessageChain()

        if (coverURL != null){
            val response = HttpRequest.get(coverURL).timeout(150_000).executeAsync().bodyStream()
            return text + text.plus(response.uploadAsImage(contact))
        }

        return text
    }
}