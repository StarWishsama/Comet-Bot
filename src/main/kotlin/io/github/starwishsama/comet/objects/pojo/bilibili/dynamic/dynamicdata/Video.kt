package io.github.starwishsama.comet.objects.pojo.bilibili.dynamic.dynamicdata

import com.google.gson.annotations.SerializedName
import io.github.starwishsama.comet.objects.WrappedMessage
import io.github.starwishsama.comet.objects.pojo.bilibili.dynamic.DynamicData

data class Video(var dynamic: String?,
                 var aid: Long?,
                 @SerializedName("pic")
                 var picURL: String?,
                 var title: String?) : DynamicData {
    override suspend fun getContact(): WrappedMessage {
        val wrapped = WrappedMessage("发布了一个视频: $title\n直达链接: https://www.bilibili.com/video/av$aid\n")

        if (picURL != null) {
            picURL.let {
                if (it != null) {
                    wrapped.picture = it
                }
            }
        }
        return wrapped
    }

}