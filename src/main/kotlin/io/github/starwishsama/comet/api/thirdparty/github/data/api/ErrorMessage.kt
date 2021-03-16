package io.github.starwishsama.comet.api.thirdparty.github.data.api

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * [ErrorMessage]
 *
 * 调用 Github API 时发生异常时返回的 Json
 */
data class ErrorMessage(
    val message: String,
    @JsonProperty("documentation_url")
    val documentationUrl: String
) {
    private val notFound = "Not Found"

    fun isNotFound(): Boolean {
        return message == notFound
    }
}