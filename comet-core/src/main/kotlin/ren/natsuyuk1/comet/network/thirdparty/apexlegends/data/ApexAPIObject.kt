package ren.natsuyuk1.comet.network.thirdparty.apexlegends.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import ren.natsuyuk1.comet.api.message.MessageWrapper
import ren.natsuyuk1.comet.api.message.asURLImage
import ren.natsuyuk1.comet.api.message.buildMessageWrapper

@Serializable
data class ApexPlayerInfo(
    val error: JsonElement? = null,
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
    appendTextln("${global.name} | ${global.level} 级")
    appendLine()

    if (global.battlePass.level > 0) {
        appendTextln("通行证等级 >> ${global.battlePass.level}")
    }

    if (global.rank.rankName != "Unranked") {
        appendTextln("本赛季段位 >> ${global.rank.rankName} ${global.rank.rankDiv}")
        appendElement(global.rank.rankImg.asURLImage())
    } else {
        appendText("未定级")
    }
}

@Serializable
data class ApexIDInfo(
    val error: JsonElement? = null,
    val name: String,
    val uid: String
)
