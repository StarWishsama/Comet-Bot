package io.github.starwishsama.nbot.objects.pojo.bilibili.dynamic.dynamicdata

import com.google.gson.annotations.SerializedName
import io.github.starwishsama.nbot.objects.WrappedMessage
import io.github.starwishsama.nbot.objects.pojo.bilibili.dynamic.DynamicData

data class Music(var id: Long,
                 @SerializedName("cover")
                 var coverURL: String?,
                 @SerializedName("intro")
                 var dynamic: String) : DynamicData {
    override suspend fun getContact(): WrappedMessage {
        val wrapped = WrappedMessage("发布了音乐 $dynamic\n")

        coverURL.let {
            if (it != null) {
                wrapped.picture = it
            }
        }

        return wrapped
    }
}