package ren.natsuyuk1.comet.network.thirdparty.arcaea.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import ren.natsuyuk1.comet.api.message.MessageWrapper
import ren.natsuyuk1.comet.api.message.buildMessageWrapper
import ren.natsuyuk1.comet.network.thirdparty.arcaea.ArcaeaHelper
import ren.natsuyuk1.comet.network.thirdparty.arcaea.formatDifficulty
import ren.natsuyuk1.comet.network.thirdparty.arcaea.formatScore
import ren.natsuyuk1.comet.network.thirdparty.arcaea.formatType
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
        val recentPlayScore: List<ArcaeaSongInfo.ArcaeaSongResult>?,
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
    )

    fun getMessageWrapper(): MessageWrapper = buildMessageWrapper {
        appendText("${data.name} | ${data.userCode}", true)
        appendText("当前 ptt >> ${getActualPtt()}", true)

        if (!data.recentPlayScore.isNullOrEmpty()) {
            appendLine()
            val lastPlay = data.recentPlayScore.first()
            appendText("最近游玩 >>", true)
            appendText("${ArcaeaHelper.getSongNameByID(lastPlay.songID)} (${lastPlay.difficulty.formatDifficulty()})", true)
            appendText("${lastPlay.score} [${lastPlay.score.formatScore()} | ${lastPlay.constant}] | ${lastPlay.clearType.formatType()}")
            appendLine()
        }

        if (!data.ratingRecord.isEmpty()) {
            appendLine()
            val lastPtt = data.ratingRecord[data.ratingRecord.size - 2].jsonArray.last().jsonPrimitive.content.toDouble()
            appendText("距上次游玩 ptt 已变化 ${((data.rating - lastPtt) / 100.0).fixDisplay()}")
        }
    }


    fun getActualPtt(): String = (data.rating / 100.0).fixDisplay()
}
