package ren.natsuyuk1.comet.network.thirdparty.arcaea.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import ren.natsuyuk1.comet.api.message.MessageWrapper
import ren.natsuyuk1.comet.api.message.buildMessageWrapper
import ren.natsuyuk1.comet.network.thirdparty.arcaea.ArcaeaClient
import ren.natsuyuk1.comet.utils.math.NumberUtil.fixDisplay

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
        val recentPlayScore: List<ArcaeaPlayResult>?,
        @SerialName("character")
        val character: Int,
        @SerialName("join_date")
        val firstPlayDate: Long,
        @SerialName("rating")
        val rating: Int,
        @SerialName("user_code")
        val userCode: String,
        @SerialName("rating_records")
        val ratingRecord: JsonArray
    ) {
        @Serializable
        data class ArcaeaPlayResult(
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
            @SerialName("clear_type")
            val clearType: Int,
            @SerialName("rating")
            val rating: Double
        )
    }

    fun getMessageWrapper(): MessageWrapper = buildMessageWrapper {
        appendText("${data.name} | ${data.userCode}", true)
        appendText("当前 ptt >> ${getActualPtt()}", true)

        if (!data.recentPlayScore.isNullOrEmpty()) {
            appendLine()
            val lastPlay = data.recentPlayScore.first()
            appendText("最近游玩 >> ${ArcaeaClient.getSongNameByID(lastPlay.songID)} (${lastPlay.difficulty.formatDifficulty()}) | ${lastPlay.clearType.formatType()}")
        }

        if (!data.ratingRecord.isEmpty()) {
            appendLine()
            val lastPtt = data.ratingRecord[data.ratingRecord.size - 2].jsonArray.last().jsonPrimitive.content.toDouble()
            appendText("距上次查询 ptt 已变化 >> ${((data.rating - lastPtt) / 100.0).fixDisplay()}")
        }
    }

    private fun Int.formatType(): String =
        when (this) {
            // 4 简单型角色, 5 困难型角色
            0, 4, 5 -> "FAILED"
            1 -> "TC"
            2 -> "FR"
            3 -> "PM"
            else -> "UNKNOWN ($this)"
        }

    private fun Int.formatDifficulty(): String =
        when (this) {
            1 -> "PST"
            2 -> "PRS"
            3 -> "FTR"
            4 -> "BYD"
            else -> "Unknown ($this)"
        }

    fun getActualPtt(): String = (data.rating / 100.0).fixDisplay()
}
