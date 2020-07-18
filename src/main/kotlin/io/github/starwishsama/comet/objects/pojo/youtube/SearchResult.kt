package io.github.starwishsama.comet.objects.pojo.youtube

import com.google.gson.annotations.SerializedName

data class SearchResult(
    val kind: String,
    val etag: String,
    val nextPageToken: String,
    val regionCode: String,
    val pageInfo: PageInfo,
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
            val channelTitle: String,
            @SerializedName("liveBroadcastContent")
            val contentType: String,
            val publishTime: String
        )
    }
}