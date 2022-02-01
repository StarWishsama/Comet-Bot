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
import io.github.starwishsama.comet.CometVariables.hmsPattern
import io.github.starwishsama.comet.api.thirdparty.bilibili.data.dynamic.DynamicData
import io.github.starwishsama.comet.objects.wrapper.MessageWrapper
import io.github.starwishsama.comet.objects.wrapper.Picture
import io.github.starwishsama.comet.utils.NumberUtil.toLocalDateTime

data class Article(
    @JsonProperty("id")
    val id: Long,
    @JsonProperty("title")
    val title: String,
    @JsonProperty("summary")
    val summary: String,
    @JsonProperty("author")
    val author: Author,
    @JsonProperty("image_urls")
    val imageURL: List<String>,
    @JsonProperty("publish_time")
    val publishTime: Long,
    @JsonProperty("stats")
    val stats: Stats,
    @JsonProperty("words")
    val wordLength: Long
) : DynamicData() {

    data class Author(
        val mid: Long,
        val name: String,
        val face: String
    )

    data class Stats(
        val view: Long,
        val favorite: Long,
        val like: Long,
        val dislike: Long,
        val reply: Long,
        val share: Long,
        val coin: Long,
        val dynamic: Int
    )

    override fun asMessageWrapper(): MessageWrapper {
        val wrapped = MessageWrapper().addText(
            "${author.name} 发布了专栏 $title:\n" +
                    "$summary\n" +
                    "查看全文: https://www.bilibili.com/read/cv/$id\n" +
                    "\uD83D\uDC4D ${stats.like}|\uD83D\uDD01 ${stats.share}|🕘 ${hmsPattern.format(publishTime.toLocalDateTime())}"
        )

        if (imageURL.isNotEmpty()) {
            wrapped.addElement(Picture(imageURL[0]))
        }
        return wrapped
    }
}