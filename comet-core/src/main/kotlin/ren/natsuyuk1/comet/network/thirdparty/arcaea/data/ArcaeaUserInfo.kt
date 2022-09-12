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
            val rating: Double,
            @SerialName("constant")
            val constant: String,
        )
    }

    fun getMessageWrapper(): MessageWrapper = buildMessageWrapper {
        appendText("${data.name} | ${data.userCode}", true)
        appendText("当前 ptt >> ${getActualPtt()}", true)

        if (!data.recentPlayScore.isNullOrEmpty()) {
            appendLine()
            val lastPlay = data.recentPlayScore.first()
            appendText("最近游玩 >>", true)
            appendText("${ArcaeaClient.getSongNameByID(lastPlay.songID)} (${lastPlay.difficulty.formatDifficulty()})", true)
            appendText("${lastPlay.score} [${lastPlay.score.formatScore()} | ${lastPlay.constant}] | ${lastPlay.clearType.formatType()}")
            appendLine()
        }

        if (!data.ratingRecord.isEmpty()) {
            appendLine()
            val lastPtt = data.ratingRecord[data.ratingRecord.size - 2].jsonArray.last().jsonPrimitive.content.toDouble()
            appendText("距上次游玩 ptt 已变化 ${((data.rating - lastPtt) / 100.0).fixDisplay()}")
        }
    }

    private fun Int.formatType(): String =
        when (this) {
            // 4 简单型角色, 5 困难型角色
            0 -> "FAILED"
            1 -> "Clear"
            4 -> "Easy Clear"
            5 -> "Hard Clear"
            2 -> "Full Recall"
            3 -> "Pure Memory"
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

    private fun Int.formatScore(): String =
        when {
            this in 8600000..8899999 -> "C"
            this in 8900000..9199999 -> "B"
            this in 9200000..9499999 -> "A"
            this in 9500000..9799999 -> "AA"
            this in 9800000..9899999 -> "EX"
            this >= 9900000 -> "EX+"
            this in 0..8599999 -> "D"
            else -> "UNKNOWN"
        }


    fun getActualPtt(): String = (data.rating / 100.0).fixDisplay()
}
