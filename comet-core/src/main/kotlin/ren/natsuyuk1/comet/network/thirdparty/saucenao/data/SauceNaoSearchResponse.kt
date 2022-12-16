package ren.natsuyuk1.comet.network.thirdparty.saucenao.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import ren.natsuyuk1.comet.api.message.MessageWrapper
import ren.natsuyuk1.comet.api.message.asURLImage
import ren.natsuyuk1.comet.api.message.buildMessageWrapper
import ren.natsuyuk1.comet.utils.math.NumberUtil.fixDisplay

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
            @SerialName("pixiv_id")
            val pixivID: String? = null,
            @SerialName("da_id")
            val deviantartID: String? = null,
            @SerialName("as_project")
            val artStationID: String? = null,
            @SerialName("danbooru_id")
            val danbooruID: String? = null,
        )
    }
}

fun SauceNaoSearchResponse.toMessageWrapper(picMode: Boolean = true): MessageWrapper = buildMessageWrapper {
    if (header.status < 0) {
        appendText("无法识别你发出的图片.")
        return@buildMessageWrapper
    } else if (header.status > 0) {
        appendText("SauceNao 服务器异常, 请稍后再试.")
        return@buildMessageWrapper
    }

    if (!results.any { it.header.similarity >= 60 }) {
        appendText("找不到该图片的以图识图结果, 相似度过低.")
        return@buildMessageWrapper
    }

    if (results.isEmpty()) {
        appendText("无法找到该图片的以图搜图结果.")
        return@buildMessageWrapper
    }

    val highestProbResult = results.first()

    appendTextln("✔ 已找到可能的图片来源")
    appendLine()
    appendTextln("🤖 相似度 ${highestProbResult.header.similarity.fixDisplay()}%")

    highestProbResult.apply {
        // Check website ID
        when {
            // Pixiv
            highestProbResult.data.pixivID != null -> {
                appendTextln("🏷 来自 Pixiv 的作品 (${highestProbResult.data.pixivID})")
                appendText("🔗 https://www.pixiv.net/artworks/${highestProbResult.data.pixivID}")
            }

            highestProbResult.data.deviantartID != null -> {
                appendTextln("🏷 来自 Deviantart 的作品")
                appendText("🔗 https://deviantart.com/view/${highestProbResult.data.deviantartID}")
            }

            highestProbResult.data.artStationID != null -> {
                appendTextln("🏷 来自 ArtStation 的作品")
                appendText("🔗 https://www.artstation.com/artwork/${highestProbResult.data.artStationID}")
            }

            highestProbResult.data.danbooruID != null -> {
                appendTextln("🏷 来自 Danbooru 的作品")
                appendText("🔗 https://danbooru.donmai.us/post/show/${highestProbResult.data.danbooruID}")
            }

            !highestProbResult.data.externalURLs.isNullOrEmpty() -> {
                appendText("可能的原作地址 🔗 ${highestProbResult.data.externalURLs.first()}")
            }

            else -> {
                appendText("找到了结果, 但是并不能解析 SauceNao 的这个结果捏🤨")
                return@apply
            }
        }

        if (picMode) {
            appendTextln("")
            appendElement(highestProbResult.header.thumbnail.asURLImage())
        }
    }
}
