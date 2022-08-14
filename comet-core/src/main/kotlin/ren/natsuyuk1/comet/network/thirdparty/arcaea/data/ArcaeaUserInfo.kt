package ren.natsuyuk1.comet.network.thirdparty.arcaea.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray

@Serializable
data class ArcaeaUserInfo(
    @SerialName("cmd")
    val command: ArcaeaCommand,
    @SerialName("data")
    val data: Data
) {
    @Serializable
    data class Data(
        @SerialName("user_id")
        val userID: Long,
        @SerialName("name")
        val name: String,
        @SerialName("recent_score")
        val recentPlayScore: List<ArcaeaPlayResult>,
        @SerialName("character")
        val character: Int,
        @SerialName("join_date")
        val firstPlayDate: Long,
        @SerialName("rating")
        val rating: Int,
        @SerialName("user_code")
        val userCode: Long,
        @SerialName("rating_records")
        val ratingRecord: JsonArray
    ) {
        @Serializable
        data class ArcaeaPlayResult(
            @SerialName("song_id")
            val songName: String,
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
            @SerialName("clear_type")
            val clearType: Int,
            @SerialName("rating")
            val rating: Double
        )
    }
}
