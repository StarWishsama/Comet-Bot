/*
 * Copyright (c) 2019-2021 StarWishsama.
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
import io.github.starwishsama.comet.utils.NumberUtil.toLocalDateTime
import java.time.LocalDateTime

data class MiniVideo(
    val item: Item?,
    val user: AuthorProfile
) : DynamicData {
    data class AuthorProfile(
        val uid: Int,
        @JsonProperty("name")
        val userName: String?,
        @JsonProperty("head_url")
        val avatarImgURL: String?
    )

    data class Item(
        var id: Long,
        var description: String?,
        var cover: Cover?,
        @JsonProperty("timestamp")
        val sentTimestamp: Long
    ) {
        data class Cover(
            @JsonProperty("default")
            val defaultImgURL: String?,
            @JsonProperty("unclipped")
            val originImgURL: String?
        )
    }

    override fun asMessageWrapper(): MessageWrapper {
        val wrapped = MessageWrapper().addText("发了一个小视频: ${item?.description ?: "获取失败"}\n")

        item?.cover?.originImgURL.let {
            if (it != null) {
                try {
                    wrapped.addPictureByURL(it)
                } catch (e: UnsupportedOperationException) {
                    return@let
                }
            }
        }

        return wrapped
    }

    override fun getSentTime(): LocalDateTime = item?.sentTimestamp?.toLocalDateTime() ?: LocalDateTime.MIN
}