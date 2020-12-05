package io.github.starwishsama.comet.objects.pojo

import com.google.gson.annotations.SerializedName

data class Hitokoto(
        @SerializedName("hitokoto")
        val content: String?,
        @SerializedName("from")
        val source: String?,
        @SerializedName("from_who")
        val author: String?
) {
        override fun toString(): String {
                return "今日一言:\n" + "$content ——${author ?: "无"}(${source ?: "无"})"
        }
}