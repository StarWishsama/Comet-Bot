package io.github.starwishsama.comet.api.bilibili.data.dynamic.dynamicdata

import com.google.gson.annotations.SerializedName
import io.github.starwishsama.comet.api.bilibili.data.dynamic.DynamicData
import io.github.starwishsama.comet.objects.MessageWrapper

data class Video(var dynamic: String?,
                 var aid: Long?,
                 @SerializedName("pic")
                 var picURL: String?,
                 var title: String?) : DynamicData {
    override suspend fun getContact(): MessageWrapper {
        val wrapped = MessageWrapper("发布了一个视频: $title\n直达链接: https://www.data.com/video/av$aid\n")

        if (picURL != null) {
            picURL.let {
                if (it != null) {
                    wrapped.picUrl = it
                }
            }
        }
        return wrapped
    }

}