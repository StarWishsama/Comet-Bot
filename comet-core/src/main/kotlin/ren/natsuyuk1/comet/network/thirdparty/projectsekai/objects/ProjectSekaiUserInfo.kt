package ren.natsuyuk1.comet.network.thirdparty.projectsekai.objects

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import ren.natsuyuk1.comet.api.message.MessageWrapper
import ren.natsuyuk1.comet.api.message.buildMessageWrapper
import ren.natsuyuk1.comet.objects.pjsk.local.ProjectSekaiMusic

/**
 * [ProjectSekaiUserInfo]
 *
 * 代表一个玩家的 `Project Sekai` 游戏数据.
 */
@Serializable
data class ProjectSekaiUserInfo(
    // 当前综合力数据
    val totalPower: TotalPower,
    // 用户信息
    val user: UserGameData,
    val userProfile: UserProfile,
    // 用户编队信息
    val userDeck: UserDeck,
    // 用户卡面信息
    val userCards: List<UserCard>,
    // 用户当前佩戴徽章信息
    val userHonors: List<UserHonor>,
    @SerialName("userMusicDifficultyClearCount") val userMusicDifficultyClearInfo: List<MusicDifficultyClearInfo>,
) {
    @Serializable
    data class TotalPower(
        val areaItemBonus: Int,
        val basicCardTotalPower: Int,
        val characterRankBonus: Int,
        val honorBonus: Int,
        val totalPower: Int,
    )

    @Serializable
    data class UserGameData(
        @SerialName("userId") val userID: Long,
        val name: String,
        @SerialName("rank") val level: Int,
    )

    @Serializable
    data class UserProfile(
        @SerialName("userId") val userID: Long,
        @SerialName("word") val bio: String,
        @SerialName("twitterId") val twitterID: String,
    )

    @Serializable
    data class MusicDifficultyClearInfo(
        val allPerfect: Int,
        val fullCombo: Int,
        val liveClear: Int,
        val musicDifficultyType: MusicDifficulty,
    )

    @Serializable
    data class UserDeck(
        // 队伍编号
        val deckId: Int,
        // 领队
        val leader: Int,
        // 领队
        val member1: Int,
        val member2: Int,
        val member3: Int,
        val member4: Int,
        val member5: Int,
        // 队伍名
        val name: String,
        // 等价于 member2
        val subLeader: Int,
        val userId: Long,
    )

    @Serializable
    data class UserCard(
        val cardId: Int,
        val defaultImage: String,
        val level: Int,
        val masterRank: Int,
        val specialTrainingStatus: String,
    )

    @Serializable
    data class UserHonor(
        val honorId: Int,
        val level: Int,
    )
}

/**
 * [MusicDifficulty]
 *
 * 代表玩家游玩曲的难度.
 */
@Serializable
enum class MusicDifficulty {
    @SerialName("easy")
    EASY,

    @SerialName("normal")
    NORMAL,

    @SerialName("hard")
    HARD,

    @SerialName("expert")
    EXPERT,

    @SerialName("master")
    MASTER,
}

fun ProjectSekaiUserInfo.toMessageWrapper(): MessageWrapper = buildMessageWrapper {
    appendTextln("${user.name} | ${user.level} 级")
    appendLine()
    if (userProfile.bio.isNotBlank()) {
        appendTextln(userProfile.bio)
        appendLine()
    }
    appendTextln("歌曲游玩情况 >>")

    userMusicDifficultyClearInfo.find { it.musicDifficultyType == MusicDifficulty.EXPERT }?.let { ex ->
        appendText(
            "EXPERT | Clear ${
                ex.liveClear
            }/${ProjectSekaiMusic.activeMusicCount} / FC ${
                ex.fullCombo
            }/ AP ${ex.allPerfect}",
        )
        appendLine()
    }

    userMusicDifficultyClearInfo.find { it.musicDifficultyType == MusicDifficulty.MASTER }?.let { ma ->
        appendText(
            "MASTER | Clear ${
                ma.liveClear
            }/${ProjectSekaiMusic.activeMusicCount} / FC ${
                ma.fullCombo
            }/ AP ${ma.allPerfect}",
        )
    }
}
