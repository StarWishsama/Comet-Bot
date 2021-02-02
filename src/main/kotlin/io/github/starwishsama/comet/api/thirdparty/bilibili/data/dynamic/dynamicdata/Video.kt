package io.github.starwishsama.comet.api.thirdparty.bilibili.data.dynamic.dynamicdata

import com.google.gson.annotations.SerializedName
import io.github.starwishsama.comet.BotVariables
import io.github.starwishsama.comet.api.thirdparty.bilibili.data.dynamic.DynamicData
import io.github.starwishsama.comet.objects.wrapper.MessageWrapper
import io.github.starwishsama.comet.utils.NumberUtil.toLocalDateTime
import java.time.LocalDateTime

data class Video(
        @SerializedName("aid")
        val avId: Long,
        @SerializedName("attribute")
        val attribute: Int,
        @SerializedName("cid")
        val cid: Long,
        /** 是否为原创视频 */
        @SerializedName("copyright")
        val isOriginalContent: Int,
        @SerializedName("ctime")
        val cTime: Long,
        /** 视频简介 */
        @SerializedName("desc")
        val description: String,
        @SerializedName("dynamic")
        val dynamicText: String,
        @SerializedName("jump_url")
        val appJumpUrl: String,
        @SerializedName("owner")
        val uploader: Uploader,
        @SerializedName("pic")
        val cover: String,
        @SerializedName("pubdate")
        val publishTime: Long,
        @SerializedName("stat")
        val stats: Stats,
        @SerializedName("title")
        val title: String

) : DynamicData {
    data class Stats(
            val aid: Long,
            val coin: Long,
            val danmaku: Long,
            val dislike: Long,
            val favorite: Long,
            @SerializedName("his_rank")
            val hisRank: Int,
            @SerializedName("like")
            val like: Long,
            @SerializedName("now_rank")
            val currentRank: Int,
            @SerializedName("reply")
            val reply: Long,
            @SerializedName("share")
            val share: Long,
            @SerializedName("view")
            val view: Long
    )

    data class Uploader(
            val face: String,
            val mid: Long,
            val name: String
    )

    override fun getContact(): MessageWrapper {
        return MessageWrapper("投递了一个视频: $title\n" +
                "发布时间: ${BotVariables.yyMMddPattern.format(publishTime.toLocalDateTime())}\n" +
                "直达链接: https://www.bilibili.com/video/av$avId\n")
                .plusImageUrl(cover)
    }

    override fun getSentTime(): LocalDateTime = publishTime.toLocalDateTime()
}