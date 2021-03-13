package io.github.starwishsama.comet.api.thirdparty.bilibili.data.dynamic.dynamicdata

import com.fasterxml.jackson.annotation.JsonProperty
import io.github.starwishsama.comet.api.thirdparty.bilibili.data.dynamic.DynamicData
import io.github.starwishsama.comet.objects.wrapper.MessageWrapper
import java.time.LocalDateTime

data class ShareContext(var vest: VestBean, var sketch: SketchBean) : DynamicData {
    data class VestBean(var uid: Long, var context: String)
    data class SketchBean(var title: String?,
                          @JsonProperty("desc_text")
                          var descText: String?,
                          @JsonProperty("cover_url")
                          var coverUrl: String?,
                          @JsonProperty("target_url")
                          var targetUrl: String?)

    override fun getContact(): MessageWrapper {
        val wrapped = MessageWrapper().addText("分享了 ${vest.context}\n")
        if (!sketch.coverUrl.isNullOrEmpty()) {
            sketch.coverUrl.let {
                if (it != null) {
                    wrapped.addPictureByURL(it)
                }
            }
        }
        return wrapped
    }

    override fun getSentTime(): LocalDateTime {
        TODO("Not yet implemented")
    }
}