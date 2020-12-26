package io.github.starwishsama.comet.api.thirdparty.bilibili.data.video

import com.google.gson.annotations.SerializedName
import com.hiczp.bilibili.api.app.model.View

data class VideoInfo(
    val code: Int,
    val message: String,
    val ttl: Int,
    val data: Data
) {
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