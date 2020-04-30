package io.github.starwishsama.nbot.objects.bilibili.dynamic.dynamicdata

import com.google.gson.annotations.SerializedName
import io.github.starwishsama.nbot.objects.bilibili.dynamic.DynamicData

data class TextWithPicture(var item: ItemBean?) : DynamicData {
    data class ItemBean(@SerializedName("description")
                        var text: String,
                        @SerializedName("pictures")
                        var pictures: List<Picture>){
        data class Picture(@SerializedName("img_src")
                           var imgUrl: String
        )
    }

    override suspend fun getContact(): List<String> {
        val list = arrayListOf("发布了动态:\n ${item?.text}\n")

        if (!item?.pictures.isNullOrEmpty()){
            item?.pictures?.get(0)?.imgUrl?.let { list.add(it) }
        }

        return list
    }
}