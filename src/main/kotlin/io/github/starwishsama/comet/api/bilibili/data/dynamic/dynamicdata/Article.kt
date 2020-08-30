package io.github.starwishsama.comet.api.bilibili.data.dynamic.dynamicdata

import com.google.gson.annotations.SerializedName
import io.github.starwishsama.comet.api.bilibili.data.dynamic.DynamicData
import io.github.starwishsama.comet.objects.MessageWrapper

data class Article(@SerializedName("title")
                   val title: String,
                   @SerializedName("image_urls")
                   val imageURL: List<String>?,
                   @SerializedName("id")
                   val id: Long,
                   @SerializedName("dynamic")
                   val context: String) : DynamicData {
    override suspend fun getContact(): MessageWrapper {
        val wrapped = MessageWrapper("专栏 $title: $context\n查看全文: https://www.data.com/read/cv/$id\n")
        if (!imageURL.isNullOrEmpty()) {
            wrapped.picUrl = imageURL[0]
        }
        return wrapped
    }
}