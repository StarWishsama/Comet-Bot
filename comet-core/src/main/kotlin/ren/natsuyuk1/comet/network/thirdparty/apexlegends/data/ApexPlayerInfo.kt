package ren.natsuyuk1.comet.network.thirdparty.apexlegends.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import ren.natsuyuk1.comet.api.message.MessageWrapper
import ren.natsuyuk1.comet.api.message.asURLImage
import ren.natsuyuk1.comet.api.message.buildMessageWrapper

@Serializable
data class ApexPlayerInfo(
    val global: ApexPlayerGlobalInfo
) {
    @Serializable
    data class ApexPlayerGlobalInfo(
        val name: String,
        val uid: String,
        val avatar: String,
        val platform: String,
        val level: Int,
        @SerialName("bans")
        val ban: BanInfo,
        val rank: RankInfo,
        val arena: RankInfo,
        @SerialName("battlepass")
        val battlePass: BattlePassInfo,
    ) {
        @Serializable
        data class BanInfo(
            val isActive: Boolean,
            val remainingSeconds: Int,
            @SerialName("last_banReason")
            val lastBanReason: String,
        )

        @Serializable
        data class RankInfo(
            val rankScore: Int,
            val rankName: String,
            @SerialName("rankDiv")
            val rankDiv: Int,
            val rankImg: String,
            val rankedSeason: String,
        )

        @Serializable
        data class BattlePassInfo(
            val level: Int,
            // val history: JsonObject
        )
    }
}

fun ApexPlayerInfo.toMessageWrapper(): MessageWrapper = buildMessageWrapper {
    appendText("${global.name} | ${global.level}", true)
    appendLine()

    if (global.rank.rankName != "Unranked") {
        appendText("本赛季段位 >> ${global.rank.rankName} ${global.rank.rankDiv}", true)
        appendElement(global.rank.rankImg.asURLImage())
    }
}
