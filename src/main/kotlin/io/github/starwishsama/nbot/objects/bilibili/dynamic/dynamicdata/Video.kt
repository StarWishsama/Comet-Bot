package io.github.starwishsama.nbot.objects.bilibili.dynamic.dynamicdata

import com.google.gson.annotations.SerializedName
import io.github.starwishsama.nbot.objects.bilibili.dynamic.DynamicData

data class Video(var dynamic: String?,
                 var aid: Long?,
                 @SerializedName("pic")
                 var picURL: String?,
                 var title: String?) : DynamicData {
    override suspend fun getContact(): List<String> {
        val list = arrayListOf("发布了一个视频: $title\n直达链接: https://www.bilibili.com/video/$aid\n")

        if (picURL != null){
            picURL.let {
                if (it != null) {
                    list.add(it)
                }
            }
        }
        return list
    }

}