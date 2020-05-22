package io.github.starwishsama.nbot.objects.pojo.bilibili.dynamic.dynamicdata

import com.google.gson.annotations.SerializedName
import io.github.starwishsama.nbot.objects.pojo.bilibili.dynamic.DynamicData

data class Music(var id: Long,
                 @SerializedName("cover")
                 var coverURL: String?,
                 @SerializedName("intro")
                 var dynamic: String) : DynamicData {
    override suspend fun getContact(): List<String> {
        val list = arrayListOf<String>()
        list.add("发布了音乐 $dynamic\n")

        coverURL.let {
            if (it != null) {
                list.add(it)
            }
        }

        return list
    }
}