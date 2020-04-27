package io.github.starwishsama.nbot.objects.bilibili.dynamic.dynamicdata

import cn.hutool.http.HttpRequest
import cn.hutool.http.HttpResponse
import com.google.gson.annotations.SerializedName
import io.github.starwishsama.nbot.objects.bilibili.dynamic.DynamicData
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.message.data.EmptyMessageChain
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.asMessageChain
import net.mamoe.mirai.message.data.toMessage
import net.mamoe.mirai.message.uploadAsImage

data class Article(@SerializedName("title")
                   val title: String,
                   @SerializedName("image_urls")
                   val imageURL: List<String>?,
                   @SerializedName("id")
                   val id: Long,
                   @SerializedName("dynamic")
                   val context: String) : DynamicData{
    override suspend fun getMessageChain(contact: Contact): MessageChain {
        val text = "专栏 $title: $context\n查看全文: https://www.bilibili.com/read/cv/$id".toMessage().asMessageChain()
        if (!imageURL.isNullOrEmpty()) {
            val response = HttpRequest.get(imageURL[0]).timeout(150_000).executeAsync().bodyStream()
            return text.plus(response.uploadAsImage(contact))
        }
        return text
    }
}