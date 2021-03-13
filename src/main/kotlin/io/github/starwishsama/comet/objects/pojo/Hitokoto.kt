package io.github.starwishsama.comet.objects.pojo

import com.fasterxml.jackson.annotation.JsonProperty

data class Hitokoto(
        @JsonProperty("hitokoto")
        val content: String?,
        @JsonProperty("from")
        val source: String?,
        @JsonProperty("from_who")
        val author: String?
) {
        override fun toString(): String {
                return "今日一言:\n" + "$content ——${author ?: "无"}(${source ?: "无"})"
        }
}