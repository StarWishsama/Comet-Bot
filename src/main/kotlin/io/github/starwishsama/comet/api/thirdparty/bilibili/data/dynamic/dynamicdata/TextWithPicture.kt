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
import com.fasterxml.jackson.databind.JsonNode
import io.github.starwishsama.comet.CometVariables
import io.github.starwishsama.comet.api.thirdparty.bilibili.data.dynamic.DynamicData
import io.github.starwishsama.comet.api.thirdparty.bilibili.data.user.UserProfile
import io.github.starwishsama.comet.objects.wrapper.MessageWrapper
import io.github.starwishsama.comet.utils.NumberUtil.toLocalDateTime

data class TextWithPicture(
    val item: Item,
    val user: User,
) : DynamicData() {
    data class User(
        val uid: Long,
        @JsonProperty("head_url")
        val headUrl: String,
        @JsonProperty("name")
        val name: String,
        @JsonProperty("vip")
        val vipInfo: UserProfile.VipInfo
    )

    data class Item(
        @JsonProperty("at_control")
        val atControl: String,
        @JsonProperty("category")
        val category: String,
        @JsonProperty("description")
        val text: String?,
        @JsonProperty("id")
        val id: Long,
        @JsonProperty("is_fav")
        val isFavorite: Int,
        @JsonProperty("pictures")
        val pictures: List<Picture>,
        @JsonProperty("pictures_count")
        val pictureCount: Int,
        @JsonProperty("reply")
        val replyCount: Long,
        @JsonProperty("role")
        val role: JsonNode,
        @JsonProperty("settings")
        val settings: JsonNode,
        @JsonProperty("source")
        val sources: JsonNode,
        @JsonProperty("title")
        val title: String,
        @JsonProperty("upload_time")
        val uploadTime: Long,
    ) {
        data class Picture(
            @JsonProperty("img_src")
            var imgUrl: String
        )
    }

    override fun asMessageWrapper(): MessageWrapper {
        val wrapped =
            MessageWrapper().addText(
                "${user.name} 发布了动态:\n ${item.text ?: "获取失败"}\n" + "🕘 ${
                    CometVariables.hmsPattern.format(
                        item.uploadTime.toLocalDateTime()
                    )
                }\n"
            )

        if (item.pictures.isNotEmpty()) {
            item.pictures.forEach {
                wrapped.addPictureByURL(it.imgUrl)
            }
        }

        return wrapped
    }
}