package io.github.starwishsama.comet.objects.pojo.youtube

import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName

data class SearchVideoResult(
        val kind: String,
        val etag: String,
        val nextPageToken: String?,
        val regionCode: String?,
        val pageInfo: PageInfo?,
        val items: List<SearchResultItem>
) {
    data class PageInfo(val totalResults: Int, val resultsPerPage: Int)

    data class SearchResultItem(
        val kind: String,
        val etag: String,
        val id: VideoId,
        val snippet: Snippet
    ) {
        data class VideoId(val kind: String, val videoId: String)

        /** 片段 */
        data class Snippet(
                /** 发布/开播时间 */
                val publishedAt: String,
                /** 频道ID */
                val channelId: String,
                @SerializedName("title")
                /** 片段标题 */
                val videoTitle: String,
                @SerializedName("description")
                /** 片段信息 */
                val desc: String,
                /** 片段封面 */
                val thumbnails: JsonObject,
                val channelTitle: String,
                @SerializedName("liveBroadcastContent")
                /** 片段类型 */
                val contentType: String,
                val publishTime: String
        ) {
            /** 获取该片段类型 */
            fun getType(): VideoType {
                return when (contentType) {
                    "live" -> return VideoType.STREAMING
                    "upcoming" -> return VideoType.UPCOMING
                    "none" -> return VideoType.VIDEO
                    else -> VideoType.UNKNOWN
                }
            }

            /** 获取该片段封面 */
            fun getCoverImgUrl(): String? {
                return try {
                    thumbnails.get("medium").asJsonObject.get("url").asString
                } catch (e: Exception) {
                    null
                }
            }
        }

        /** 获取该片段链接 */
        fun getVideoUrl(): String {
            return "https://www.youtube.com/watch?v=${id.videoId}"
        }
    }
}

enum class VideoType {
    /** 视频 */
    VIDEO,

    /** 正在直播 */
    STREAMING,

    /** 即将开始的直播 */
    UPCOMING,

    /** 未知类型 */
    UNKNOWN
}