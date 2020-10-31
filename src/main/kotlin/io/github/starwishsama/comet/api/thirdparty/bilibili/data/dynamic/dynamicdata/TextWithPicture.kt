package io.github.starwishsama.comet.api.thirdparty.bilibili.data.dynamic.dynamicdata

import com.google.gson.annotations.SerializedName
import io.github.starwishsama.comet.api.thirdparty.bilibili.data.dynamic.DynamicData
import io.github.starwishsama.comet.objects.wrapper.MessageWrapper

data class TextWithPicture(var item: ItemBean?) : DynamicData {
    data class ItemBean(@SerializedName("description")
                        var text: String?,
                        @SerializedName("pictures")
                        var pictures: List<Picture>) {
        data class Picture(@SerializedName("img_src")
                           var imgUrl: String
        )
    }

    override suspend fun getContact(): MessageWrapper {
        val wrapped = MessageWrapper("发布了动态:\n ${item?.text ?: "无"}\n")

        if (!item?.pictures.isNullOrEmpty()) {
            item?.pictures?.get(0)?.imgUrl?.let { wrapped.plusImageUrl(it) }
        }

        return wrapped
    }
}