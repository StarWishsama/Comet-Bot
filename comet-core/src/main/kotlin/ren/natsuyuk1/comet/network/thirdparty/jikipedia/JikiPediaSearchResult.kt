package ren.natsuyuk1.comet.network.thirdparty.jikipedia

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.json.JsonElement
import ren.natsuyuk1.comet.utils.math.NumberUtil.getBetterNumber
import ren.natsuyuk1.comet.utils.message.buildMessageWrapper
import ren.natsuyuk1.comet.utils.string.StringUtil.format
import ren.natsuyuk1.comet.utils.string.StringUtil.limit

@kotlinx.serialization.Serializable
data class JikiPediaSearchRequest(
    val phrase: String,
    val page: Int = 1,
    val size: Int = 20
)

@kotlinx.serialization.Serializable
data class JikiPediaSearchResult(
    val data: List<JikiSearchEntry>,
    @SerialName("size")
    val pageSize: Int,
    val from: Int,
    val to: Int,
    val total: Int,
    val message: String
) {
    fun toMessageWrapper() =
        buildMessageWrapper {
            if (!data.any { it.category == JikiSearchCategory.DEFINITION }) {
                appendText("❌ 找不到搜索结果")
            } else {
                val definition = data.firstOrNull { it.category == JikiSearchCategory.DEFINITION }

                if (definition == null) {
                    appendText("❌ 找不到搜索结果")
                    return@buildMessageWrapper
                } else {
                    val dn = definition.definitions.first()

                    appendText(dn.term.title, true)
                    appendText("${dn.createTime.format()} | 阅读 ${dn.view.getBetterNumber()}", true)

                    if (dn.plainText.length > 100) {
                        appendText(dn.plainText.limit(100), true)
                        appendText("🔍 查看全部 https://jikipedia.com/definition/${dn.id}")
                    } else {
                        appendText(dn.plainText)
                    }
                }
            }
        }

    @kotlinx.serialization.Serializable
    data class JikiSearchEntry(
        val topics: JsonElement,
        val category: JikiSearchCategory,
        val definitions: List<JikiDefinition>
        // val albums: List<JikiAlbum>,
        // val tags: List<JikiTag>,
    )

    @kotlinx.serialization.Serializable
    data class JikiImage(
        val full: JikiImageEntry,
        val scaled: JikiImageEntry
    ) {
        @kotlinx.serialization.Serializable
        data class JikiImageEntry(
            val id: Long,
            val path: String,
            val width: Int,
            val height: Int,
            val viewable: Boolean
        )
    }

    @kotlinx.serialization.Serializable
    data class JikiDefinition(
        val id: Long,
        val images: List<JikiImage>,
        @SerialName("created_at")
        val createTime: Instant,
        @SerialName("updated_at")
        val updateTime: Instant,
        val status: String,
        @SerialName("status_message")
        val statusMessage: String,
        val term: JikiTerm,
        val content: String,
        @SerialName("plaintext")
        val plainText: String,
        @SerialName("like_count")
        val like: Int,
        @SerialName("dislike_count")
        val dislike: Int,
        @SerialName("view_count")
        val view: Int
    ) {
        @kotlinx.serialization.Serializable
        data class JikiTerm(
            val id: Long,
            val title: String,
            val status: String
        )
    }

    @kotlinx.serialization.Serializable
    enum class JikiSearchCategory {
        @SerialName("definition")
        DEFINITION,

        @SerialName("album")
        ALBUM,

        @SerialName("tag")
        TAG,

        @SerialName("banner")
        BANNER,

        @SerialName("user")
        USER
    }
}
