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

data class Video(
    @JsonProperty("aid")
    val avId: Long,
    @JsonProperty("attribute")
    val attribute: Int,
    @JsonProperty("cid")
    val cid: Long,
    /** 是否为原创视频 */
    @JsonProperty("copyright")
    val isOriginalContent: Int,
    @JsonProperty("ctime")
    val cTime: Long,
    /** 视频简介 */
    @JsonProperty("desc")
    val description: String,
    @JsonProperty("dynamic")
    val dynamicText: String,
    @JsonProperty("jump_url")
    val appJumpUrl: String,
    @JsonProperty("owner")
    val uploader: Uploader,
    @JsonProperty("pic")
    val cover: String,
    @JsonProperty("pubdate")
    val publishTime: Long,
    @JsonProperty("stat")
    val stats: Stats,
    @JsonProperty("title")
    val title: String

) : DynamicData {
    data class Stats(
        val aid: Long,
        val coin: Long,
        val danmaku: Long,
        val dislike: Long,
        val favorite: Long,
        @JsonProperty("his_rank")
        val hisRank: Int,
        @JsonProperty("like")
        val like: Long,
        @JsonProperty("now_rank")
        val currentRank: Int,
        @JsonProperty("reply")
        val reply: Long,
        @JsonProperty("share")
        val share: Long,
        @JsonProperty("view")
        val view: Long
    )

    data class Uploader(
        val face: String,
        val mid: Long,
        val name: String
    )

    override fun asMessageWrapper(): MessageWrapper {
        return MessageWrapper().addText(
            "投递了一个视频: $title\n" +
                    "发布时间: ${CometVariables.yyMMddPattern.format(publishTime.toLocalDateTime())}\n" +
                    "直达链接: https://www.bilibili.com/video/av$avId\n"
        )
            .addPictureByURL(cover)
    }

    override fun getSentTime(): LocalDateTime = publishTime.toLocalDateTime()
}