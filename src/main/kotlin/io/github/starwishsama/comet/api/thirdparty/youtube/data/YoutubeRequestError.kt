package io.github.starwishsama.comet.api.thirdparty.youtube.data

data class YoutubeRequestError(
    val error: Error
) {
    data class Error(
        val code: Int,
        val message: String?,
        val errors: List<ErrorDetail>,
        val status: String?
    )

    data class ErrorDetail(
        val message: String,
        val domain: String,
        val reason: String?,
        val location: String?,
        val locationType: String?
    )
}