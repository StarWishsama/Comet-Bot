package io.github.starwishsama.nbot.objects.pojo.bilibili.dynamic.dynamicdata

import com.google.gson.annotations.SerializedName
import io.github.starwishsama.nbot.objects.WrappedMessage
import io.github.starwishsama.nbot.objects.pojo.bilibili.dynamic.DynamicData

data class Article(@SerializedName("title")
                   val title: String,
                   @SerializedName("image_urls")
                   val imageURL: List<String>?,
                   @SerializedName("id")
                   val id: Long,
                   @SerializedName("dynamic")
                   val context: String) : DynamicData{
    override suspend fun getContact(): WrappedMessage {
        val wrapped = WrappedMessage("专栏 $title: $context\n查看全文: https://www.bilibili.com/read/cv/$id\n")
        if (!imageURL.isNullOrEmpty()) {
            wrapped.picture = imageURL[0]
        }
        return wrapped
    }
}