package io.github.starwishsama.comet.objects.pojo.youtube

data class YoutubeRequestError(
    val code: Int,
    val message: String?,
    val errors: List<YoutubeError>,
    val status: String
) {
    data class YoutubeError(
        val message: String,
        val domain: String,
        val reason: String?,
        val location: String?,
        val locationType: String?
    )
}