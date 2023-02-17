package ren.natsuyuk1.comet.network.thirdparty.arcaea.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ArcaeaSongInfo(
    @SerialName("data")
    val songResult: List<ArcaeaSongResult>,
) {
    @Serializable
    data class ArcaeaSongResult(
        @SerialName("song_id")
        val songID: String,
        val difficulty: Int,
        val score: Int,
        @SerialName("shiny_perfect_count")
        val shinyPure: Int,
        @SerialName("perfect_count")
        val pure: Int,
        @SerialName("near_count")
        val far: Int,
        @SerialName("miss_count")
        val miss: Int,
        val health: Int,
        val modifier: Int,
        @SerialName("time_played")
        val playTime: Long,
        @SerialName("clear_type")
        val clearType: Int,
        @SerialName("rating")
        val rating: Double,
        @SerialName("constant")
        val constant: String,
    )
}
