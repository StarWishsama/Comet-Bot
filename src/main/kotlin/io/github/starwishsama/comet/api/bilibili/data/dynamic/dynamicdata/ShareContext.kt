package io.github.starwishsama.comet.api.bilibili.data.dynamic.dynamicdata

import com.google.gson.annotations.SerializedName
import io.github.starwishsama.comet.api.bilibili.data.dynamic.DynamicData
import io.github.starwishsama.comet.objects.MessageWrapper

data class ShareContext(var vest: VestBean, var sketch: SketchBean) : DynamicData {
    data class VestBean(var uid: Long, var context: String)
    data class SketchBean(var title: String?,
                          @SerializedName("desc_text")
                          var descText: String?,
                          @SerializedName("cover_url")
                          var coverUrl: String?,
                          @SerializedName("target_url")
                          var targetUrl: String?)

    override suspend fun getContact(): MessageWrapper {
        val wrapped = MessageWrapper("分享了 ${vest.context}\n")
        if (!sketch.coverUrl.isNullOrEmpty()) {
            sketch.coverUrl.let {
                if (it != null) {
                    wrapped.picUrl = it
                }
            }
        }
        return wrapped
    }
}