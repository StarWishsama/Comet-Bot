/*
 * Copyright (c) 2019-2022 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package io.github.starwishsama.comet.api.thirdparty.bilibili.data.dynamic.dynamicdata

import com.fasterxml.jackson.annotation.JsonProperty
import io.github.starwishsama.comet.api.thirdparty.bilibili.data.dynamic.DynamicData
import io.github.starwishsama.comet.objects.wrapper.MessageWrapper
import java.time.LocalDateTime

data class ShareContext(var vest: VestBean, var sketch: SketchBean) : DynamicData {
    data class VestBean(var uid: Long, var content: String)
    data class SketchBean(
        var title: String?,
        @JsonProperty("desc_text")
        var descText: String?,
        @JsonProperty("cover_url")
        var coverUrl: String?,
        @JsonProperty("target_url")
        var targetUrl: String?
    )

    override fun asMessageWrapper(): MessageWrapper {
        val wrapped = MessageWrapper().addText("分享了 ${vest.content}\n")
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