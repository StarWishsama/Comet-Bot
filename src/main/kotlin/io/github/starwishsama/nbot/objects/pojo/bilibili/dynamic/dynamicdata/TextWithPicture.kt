package io.github.starwishsama.nbot.objects.pojo.bilibili.dynamic.dynamicdata

import com.google.gson.annotations.SerializedName
import io.github.starwishsama.nbot.objects.WrappedMessage
import io.github.starwishsama.nbot.objects.pojo.bilibili.dynamic.DynamicData

data class TextWithPicture(var item: ItemBean?) : DynamicData {
    data class ItemBean(@SerializedName("description")
                        var text: String,
                        @SerializedName("pictures")
                        var pictures: List<Picture>){
        data class Picture(@SerializedName("img_src")
                           var imgUrl: String
        )
    }

    override suspend fun getContact(): WrappedMessage {
        val wrapped = WrappedMessage("发布了动态:\n ${item?.text}\n")

        if (!item?.pictures.isNullOrEmpty()){
            item?.pictures?.get(0)?.imgUrl?.let { wrapped.picture = it }
        }

        return wrapped
    }
}