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
import io.github.starwishsama.comet.CometVariables
import io.github.starwishsama.comet.api.thirdparty.bilibili.data.dynamic.DynamicData
import io.github.starwishsama.comet.objects.wrapper.MessageWrapper
import io.github.starwishsama.comet.utils.NumberUtil.toLocalDateTime
import java.time.LocalDateTime

data class Music(
    @JsonProperty("id")
    val id: Long,
    @JsonProperty("upId")
    val upId: Long,
    @JsonProperty("title")
    val songName: String,
    @JsonProperty("upper")
    val uploader: String,
    @JsonProperty("cover")
    val coverURL: String?,
    @JsonProperty("author")
    val author: String,
    @JsonProperty("ctime")
    val uploadTime: Long,
    @JsonProperty("intro")
    val dynamic: String?,
    @JsonProperty("replyCnt")
    val replyCount: Long,
    @JsonProperty("playCnt")
    val playCount: Long
) : DynamicData {
    override fun asMessageWrapper(): MessageWrapper {
        return MessageWrapper().addText(
            "${dynamic ?: "获取失败"}\n" +
                    "发布了音乐: $songName\n" +
                    "🕘 ${CometVariables.yyMMddPattern.format(uploadTime.toLocalDateTime())}"
        ).apply {
            if (coverURL != null) {
                addPictureByURL(coverURL)
            }
        }
    }

    override fun getSentTime(): LocalDateTime = uploadTime.toLocalDateTime()
}