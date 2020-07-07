package io.github.starwishsama.nbot.objects.pojo.bilibili.dynamic.dynamicdata

import com.google.gson.annotations.SerializedName
import io.github.starwishsama.nbot.objects.TextPlusPicture
import io.github.starwishsama.nbot.objects.pojo.bilibili.dynamic.DynamicData

data class Music(var id: Long,
                 @SerializedName("cover")
                 var coverURL: String?,
                 @SerializedName("intro")
                 var dynamic: String) : DynamicData {
    override suspend fun getContact(): TextPlusPicture {
        val wrapped = TextPlusPicture("发布了音乐 $dynamic\n")

        coverURL.let {
            if (it != null) {
                wrapped.picture = it
            }
        }

        return wrapped
    }
}