package io.github.starwishsama.comet.objects.pojo.youtube

import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName

data class SearchUserResult(
        val kind: String,
        val etag: String,
        val pageInfo: PageInfo,
        val items: List<SearchResultItem>
) {
    data class PageInfo(val totalResults: Int, val resultsPerPage: Int)

    data class SearchResultItem(
            val kind: String,
            val etag: String,
            val id: String,
            val snippet: UserSnippet
    ) {
        data class UserSnippet(
                @SerializedName("title")
                val displayName: String,
                @SerializedName("description")
                val desc: String,
                val customUrl: String,
                @SerializedName("publishedAt")
                val createdAt: String,
                val thumbnails: JsonObject,
                val localized: JsonObject,
                val contentDetails: JsonObject,
                val statistics: Statistics
        ) {
            data class Statistics(val viewCount: String,
                                  val commentCount: String,
                                  val subscriberCount: String,
                                  val hiddenSubscriberCount: Boolean,
                                  val videoCount: String)

            fun getFaceImgUrl(): String? {
                return try {
                    thumbnails.get("medium").asJsonObject.get("url").asString
                } catch (e: Exception) {
                    null
                }
            }
        }

        fun getPageUrl(): String {
            return "https://www.youtube.com/channel/${id}"
        }
    }
}