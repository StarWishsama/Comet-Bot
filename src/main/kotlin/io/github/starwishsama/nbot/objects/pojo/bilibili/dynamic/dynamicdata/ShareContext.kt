package io.github.starwishsama.nbot.objects.pojo.bilibili.dynamic.dynamicdata

import com.google.gson.annotations.SerializedName
import io.github.starwishsama.nbot.objects.WrappedMessage
import io.github.starwishsama.nbot.objects.pojo.bilibili.dynamic.DynamicData

data class ShareContext(var vest: VestBean, var sketch: SketchBean) : DynamicData {
    data class VestBean(var uid: Long, var context: String)
    data class SketchBean(var title: String?,
                          @SerializedName("desc_text")
                          var descText: String?,
                          @SerializedName("cover_url")
                          var coverUrl: String?,
                          @SerializedName("target_url")
                          var targetUrl: String?)

    override suspend fun getContact(): WrappedMessage {
        val wrapped = WrappedMessage("分享了 ${vest.context}\n")
        if (!sketch.coverUrl.isNullOrEmpty()){
            sketch.coverUrl.let {
                if (it != null) {
                    wrapped.picture = it
                }
            }
        }
        return wrapped
    }
}