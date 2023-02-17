package ren.natsuyuk1.comet.objects.hitokito

import kotlinx.serialization.SerialName

@kotlinx.serialization.Serializable
data class Hitokoto(
    @SerialName("hitokoto")
    val content: String?,
    @SerialName("from")
    val source: String?,
    @SerialName("from_who")
    val author: String?,
) {
    override fun toString(): String {
        return "$content ——${author ?: "无"}(${source ?: "无"})"
    }
}
