package io.github.starwishsama.bilibiliapi.data.dynamic.dynamicdata

import com.google.gson.annotations.SerializedName
import io.github.starwishsama.bilibiliapi.data.dynamic.DynamicData
import io.github.starwishsama.comet.objects.MessageWrapper

data class Music(var id: Long,
                 @SerializedName("cover")
                 var coverURL: String?,
                 @SerializedName("intro")
                 var dynamic: String) : DynamicData {
    override suspend fun getContact(): MessageWrapper {
        val wrapped = MessageWrapper("发布了音乐 $dynamic\n")

        coverURL.let {
            if (it != null) {
                wrapped.picUrl = it
            }
        }

        return wrapped
    }
}