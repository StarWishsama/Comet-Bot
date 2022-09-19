package ren.natsuyuk1.comet.network.thirdparty.saucenao.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import ren.natsuyuk1.comet.api.message.MessageWrapper
import ren.natsuyuk1.comet.api.message.buildMessageWrapper

@Serializable
data class SauceNaoSearchResponse(
    val header: Header,
    val results: List<SearchResult>
) {
    @Serializable
    data class Header(
        @SerialName("user_id")
        val userID: String,
        @SerialName("account_type")
        val accountType: Int,
        @SerialName("short_limit")
        val shortLimit: Int,
        @SerialName("long_limit")
        val longLimit: Int,
        @SerialName("long_remaining")
        val longRemaining: Int,
        @SerialName("short_remaining")
        val shortRemaining: Int,
        val status: Int,
        @SerialName("minimum_similarity")
        val minimumSimilarity: Double,
        @SerialName("query_image")
        val queryImage: String
    )

    @Serializable
    data class SearchResult(
        val header: SearchHeader,
        val data: SearchData
    ) {
        @Serializable
        data class SearchHeader(
            val similarity: Double,
            val thumbnail: String,
            @SerialName("index_id")
            val indexID: Int,
            @SerialName("index_name")
            val indexName: String,
            val dupes: Int,
            val hidden: Int,
        )

        @Serializable
        data class SearchData(
            @SerialName("ext_urls")
            val externalURLs: List<String>? = null,
            val published: String? = null,
            val characters: String? = null,
            val title: String? = null,
            val source: String? = null,
        )
    }
}

fun SauceNaoSearchResponse.toMessageWrapper(): MessageWrapper = buildMessageWrapper {
    if (header.status < 0) {
        appendText("无法识别你发出的图片.")
        return@buildMessageWrapper
    } else if (header.status > 0) {
        appendText("SauceNao 服务器异常, 请稍后再试.")
        return@buildMessageWrapper
    }

    if (!results.any { it.header.similarity < 60 }) {
        appendText("找不到该图片的以图识图结果, 相似度过低.")
        return@buildMessageWrapper
    }

    if (results.isEmpty()) {
        appendText("无法找到该图片的以图搜图结果.")
        return@buildMessageWrapper
    }

    val highestProbResult = results.first()

    appendText("✔ 已找到可能的图片来源", true)

    // 画作网站独占 (Pixiv, skeb, deviantart)
    if (!highestProbResult.data.externalURLs.isNullOrEmpty()) {
        appendText("原作地址 🔗 ${highestProbResult.data.externalURLs.first()}", true)
    }
}
