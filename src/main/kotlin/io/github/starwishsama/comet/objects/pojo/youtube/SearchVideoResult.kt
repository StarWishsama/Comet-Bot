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
        data class Snippet(
                val publishedAt: String,
                val channelId: String,
                @SerializedName("title")
                val videoTitle: String,
                @SerializedName("description")
                val desc: String,
                val thumbnails: JsonObject,
                val channelTitle: String,
                @SerializedName("liveBroadcastContent")
                val contentType: String,
                val publishTime: String
        ) {
            fun getType(): VideoType {
                return when (contentType) {
                    "live" -> return VideoType.STREAMING
                    "upcoming" -> return VideoType.UPCOMING
                    "none" -> return VideoType.VIDEO
                    else -> VideoType.UNKNOWN
                }
            }

            fun getCoverImgUrl(): String? {
                return try {
                    thumbnails.get("medium").asJsonObject.get("url").asString
                } catch (e: Exception) {
                    null
                }
            }
        }

        fun getVideoUrl(): String {
            return "https://www.youtube.com/watch?v=${id.videoId}"
        }
    }
}

enum class VideoType {
    VIDEO, STREAMING, UPCOMING, UNKNOWN
}