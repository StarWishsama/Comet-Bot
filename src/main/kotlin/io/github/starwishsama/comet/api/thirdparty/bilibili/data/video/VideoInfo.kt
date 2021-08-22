/*
 * Copyright (c) 2019-2021 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package io.github.starwishsama.comet.api.thirdparty.bilibili.data.video

import com.fasterxml.jackson.annotation.JsonProperty
import io.github.starwishsama.comet.objects.wrapper.MessageWrapper
import io.github.starwishsama.comet.utils.FileUtil
import io.github.starwishsama.comet.utils.StringUtil.limitStringSize

data class VideoInfo(
    val code: Int,
    val message: String,
    val ttl: Int,
    val data: Data?
) {
    fun toMessageWrapper(): MessageWrapper {
        try {

            if (data == null) return MessageWrapper().setUsable(false)

            return MessageWrapper()
                .addText(
                    """
${data.title}
| ${data.uploader.userName}
| ${data.description.limitStringSize(80)}
| 👍 ${data.stats.like} 💰 ${data.stats.coin} ⭐ ${data.stats.favorite}
${if (data.stats.historyRank > 0) "| 本站最高日排行第${data.stats.historyRank}名" else ""}
直达链接: https://bilibili.com/video/${data.bvID}
                """.trim().removePrefix(" ")
                )
                .addPictureByURL(data.coverImg)
        } catch (e: Exception) {
            FileUtil.createErrorReportFile("解析视频消息失败", "bilibili", e, this.toString(), "")
        }

        return MessageWrapper().setUsable(false)
    }

    data class Data(
        @JsonProperty("bvid")
        val bvID: String,
        @JsonProperty("aid")
        val avID: Long,
        @JsonProperty("videos")
        val videoCount: Int,
        /**
         * 分区 ID
         */
        @JsonProperty("tid")
        val partitionID: Long,
        /**
         * 分区名
         */
        @JsonProperty("tname")
        val partitionName: String,
        /**
         * 是否为自制视频
         */
        @JsonProperty("copyright")
        val originalVideo: Int,
        @JsonProperty("pic")
        val coverImg: String,
        @JsonProperty("title")
        val title: String?,
        @JsonProperty("pubdate")
        val publishTime: Long,
        @JsonProperty("desc")
        val description: String,
        @JsonProperty("owner")
        val uploader: Uploader,
        @JsonProperty("stat")
        val stats: Stats,
        /**
         * 视频同步发布时发送动态的文字内容
         */
        @JsonProperty("dynamic")
        val dynamic: String?,
    ) {
        data class Uploader(
            @JsonProperty("mid")
            val memberID: Long,
            @JsonProperty("name")
            val userName: String,
            @JsonProperty("face")
            val avatarImageUrl: String
        )

        data class Stats(
            @JsonProperty("aid")
            val avID: Long,
            @JsonProperty("view")
            val view: Long,
            @JsonProperty("danmaku")
            val danmaku: Long,
            @JsonProperty("reply")
            val reply: Long,
            @JsonProperty("favorite")
            val favorite: Long,
            @JsonProperty("coin")
            val coin: Long,
            @JsonProperty("share")
            val share: Long,
            /**
             * 现在的全站排行
             */
            @JsonProperty("now_rank")
            val currentRank: Int,
            /**
             * 历史全站日排行
             */
            @JsonProperty("his_rank")
            val historyRank: Int,
            @JsonProperty("like")
            val like: Long,
            @JsonProperty("dislike")
            val dislike: Long
        )
    }
}