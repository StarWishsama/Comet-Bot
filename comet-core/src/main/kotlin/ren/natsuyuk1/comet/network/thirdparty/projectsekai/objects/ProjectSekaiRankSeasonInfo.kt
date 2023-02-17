package ren.natsuyuk1.comet.network.thirdparty.projectsekai.objects

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import ren.natsuyuk1.comet.utils.math.NumberUtil.fixDisplay

@Serializable
data class ProjectSekaiRankSeasonInfo(
    val rankings: List<RankSeasonInfo>,
) {
    @Serializable
    data class RankSeasonInfo(
        val userId: Long,
        val score: Int,
        val rank: Int,
        val name: String,
        val userCard: SekaiProfileEventInfo.EventInfo.UserCard,
        val userProfile: ProjectSekaiUserInfo.UserProfile,
        val userRankMatchSeason: SeasonInfo,
    ) {
        @Serializable
        data class SeasonInfo(
            val rankMatchSeasonId: Int,
            val rankMatchTierId: Int,
            val tierPoint: Int,
            val totalTierPoint: Int,
            val playCount: Int,
            val consecutiveWinCount: Int,
            val maxConsecutiveWinCount: Int,
            @SerialName("winCount")
            val win: Int,
            @SerialName("loseCount")
            val lose: Int,
            @SerialName("drawCount")
            val draw: Int,
            @SerialName("penaltyCount")
            val penalty: Int,
        )
    }

    fun getRankInfo(): String = buildString {
        if (rankings.isEmpty()) {
            append("未参加当期排位赛捏")
        } else {
            val info = rankings.first().userRankMatchSeason

            append("排位赛信息")
            appendLine()
            append("当前段位 >> ${info.rankMatchTierId.parseToPJSKRank()}")
            appendLine()
            append("Win ${info.win} | Lose ${info.lose}${if (info.draw > 0) " | Draw ${info.draw}" else ""} ")
            appendLine()
            append("最高连胜 ${info.maxConsecutiveWinCount}")
            if (info.consecutiveWinCount > 0) {
                append(" | 当前连胜 ${info.consecutiveWinCount}")
            }
            appendLine()
            append("胜率 >> ${((info.win.toDouble() / (info.win + info.lose)) * 100.0).fixDisplay()}%")
            appendLine()
        }
    }
}

private fun Int.parseToPJSKRank(): String {
    val grade = (((this - 1) / 4) + 1) - 1
    val rank = PJSKRank.values()[grade.coerceAtMost(6)]
    val subGrade = this - 4 * grade

    return "${rank.i18n} $subGrade 级"
}

enum class PJSKRank(val i18n: String) {
    BEGINNER("初学者"),
    BRONZE("青铜"),
    SILVER("白银"),
    GOLD("黄金"),
    PLATINUM("白金"),
    DIAMOND("钻石"),
    MASTER("大师"),
}
