package io.github.starwishsama.comet.api.thirdparty.bilibili.data.video

import com.google.gson.annotations.SerializedName
import com.hiczp.bilibili.api.app.model.View
import io.github.starwishsama.comet.objects.wrapper.MessageWrapper
import io.github.starwishsama.comet.utils.FileUtil

data class VideoInfo(
    val code: Int,
    val message: String,
    val ttl: Int,
    val data: Data?
) {
    fun toMessageWrapper(): MessageWrapper {
        try {

            if (data == null) return MessageWrapper(text = null, success = false)

            return MessageWrapper(
                """
                ${data.title}
                > ${data.uploader.userName}
                > ${data.description}
                👍 ${data.stats.like} 💴 ${data.stats.coin} ⭐ ${data.stats.favorite}
                ${if (data.stats.historyRank > 0) "本站最高日排行第${data.stats.historyRank}名" else ""}
            """.trimIndent()
            ).plusImageUrl(data.coverImg)
        } catch (e: Exception) {
            FileUtil.createErrorReportFile("解析视频消息失败", "bilibili", e, this.toString(), "")
        }

        return MessageWrapper(text = null, success = false)
    }

    data class Data(
        @SerializedName("bvid")
        val bvID: String,
        @SerializedName("aid")
        val avID: Long,
        @SerializedName("videos")
        val videoCount: Int,
        /**
         * 分区 ID
         */
        @SerializedName("tid")
        val partitionID: Long,
        /**
         * 分区名
         */
        @SerializedName("tname")
        val partitionName: String,
        /**
         * 是否为自制视频
         */
        @SerializedName("copyright")
        val originalVideo: Int,
        @SerializedName("pic")
        val coverImg: String,
        @SerializedName("title")
        val title: String?,
        @SerializedName("pubdate")
        val publishTime: Long,
        @SerializedName("desc")
        val description: String,
        @SerializedName("owner")
        val uploader: Uploader,
        @SerializedName("stat")
        val stats: Stats,
        /**
         * 视频同步发布时发送动态的文字内容
         */
        @SerializedName("dynamic")
        val dynamic: String?,
        @SerializedName("staff")
        val staff: List<View.Data.Staff?>
    ) {
        data class Uploader(
            @SerializedName("mid")
            val memberID: Long,
            @SerializedName("name")
            val userName: String,
            @SerializedName("face")
            val avatarImageUrl: String
        )

        data class Stats(
            @SerializedName("aid")
            val avID: Long,
            @SerializedName("view")
            val view: Long,
            @SerializedName("danmaku")
            val danmaku: Long,
            @SerializedName("reply")
            val reply: Long,
            @SerializedName("favorite")
            val favorite: Long,
            @SerializedName("coin")
            val coin: Long,
            @SerializedName("share")
            val share: Long,
            /**
             * 现在的全站排行
             */
            @SerializedName("now_rank")
            val currentRank: Int,
            /**
             * 历史全站日排行
             */
            @SerializedName("his_rank")
            val historyRank: Int,
            @SerializedName("like")
            val like: Long,
            @SerializedName("dislike")
            val dislike: Long
        )
    }
}