package ren.natsuyuk1.comet.network.thirdparty.projectsekai.objects

import kotlinx.serialization.SerialName
import ren.natsuyuk1.comet.utils.message.MessageWrapper
import ren.natsuyuk1.comet.utils.message.buildMessageWrapper

fun ProjectSekaiUserInfo.toMessageWrapper(): MessageWrapper =
    buildMessageWrapper {
        appendText("${user.userGameData.name} | ${user.userGameData.level} 级", true)
        appendLine()
        if (userProfile.bio.isNotBlank()) {
            appendText(userProfile.bio, true)
            appendLine()
        }
        appendText("歌曲游玩情况 >>", true)

        val clear = getSpecificMusicCount(MusicPlayResult.CLEAR)
        val fc = getSpecificMusicCount(MusicPlayResult.FULL_COMBO)
        val ap = getSpecificMusicCount(MusicPlayResult.ALL_PERFECT)
        appendText("Clear $clear / FC $fc / AP $ap")
    }

/**
 * [ProjectSekaiUserInfo]
 *
 * 代表一个玩家的 `Project Sekai` 游戏数据.
 */
@kotlinx.serialization.Serializable
data class ProjectSekaiUserInfo(
    val user: UserGameData,
    val userProfile: UserProfile,
    //val userDecks
    //val userCards
    val userMusics: List<UserMusic>
) {
    fun getSpecificMusicCount(playResult: MusicPlayResult): Int {
        var counter = 0

        userMusics.forEach {
            counter += it.musicStatus.count { ms ->
                ms.userMusicResult.any { umr -> umr.playResult == playResult }
            }
        }

        return counter
    }

    @kotlinx.serialization.Serializable
    data class UserGameData(
        @SerialName("userGamedata")
        val userGameData: Data
    ) {
        @kotlinx.serialization.Serializable
        data class Data(
            @SerialName("userId")
            val userID: Long,
            val name: String,
            @SerialName("rank")
            val level: Int
        )
    }

    @kotlinx.serialization.Serializable
    data class UserProfile(
        @SerialName("userId")
        val userID: Long,
        @SerialName("word")
        val bio: String,
        @SerialName("twitterId")
        val twitterID: String
    )

    @kotlinx.serialization.Serializable
    data class UserMusic(
        @SerialName("userId")
        val userID: Long,
        @SerialName("musicId")
        val musicID: Int,
        @SerialName("userMusicDifficultyStatuses")
        val musicStatus: List<MusicStatus>
    ) {
        @kotlinx.serialization.Serializable
        data class MusicStatus(
            @SerialName("musicId")
            val musicID: Int,
            val musicDifficulty: String,
            val musicDifficultyStatus: String,
            @SerialName("userMusicResults")
            val userMusicResult: List<UserMusicResult>
        ) {
            @kotlinx.serialization.Serializable
            data class UserMusicResult(
                // easy, normal, hard, expert, master
                val musicDifficulty: MusicDifficulty,
                // clear, full_combo, all_perfect
                val playResult: MusicPlayResult,
            )
        }
    }
}

/**
 * [MusicDifficulty]
 *
 * 代表玩家游玩曲的难度.
 */
@kotlinx.serialization.Serializable
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

/**
 * [MusicPlayResult]
 *
 * 代表玩家游玩过曲目的状态.
 */
@kotlinx.serialization.Serializable
enum class MusicPlayResult {
    @SerialName("clear")
    CLEAR,

    @SerialName("full_combo")
    FULL_COMBO,

    @SerialName("all_perfect")
    ALL_PERFECT
}
