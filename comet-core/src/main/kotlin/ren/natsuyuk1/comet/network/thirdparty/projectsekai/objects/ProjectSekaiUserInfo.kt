package ren.natsuyuk1.comet.network.thirdparty.projectsekai.objects

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import ren.natsuyuk1.comet.api.message.Image
import ren.natsuyuk1.comet.api.message.MessageWrapper
import ren.natsuyuk1.comet.api.message.buildMessageWrapper
import ren.natsuyuk1.comet.service.ProjectSekaiManager
import ren.natsuyuk1.comet.service.image.ProjectSekaiImageService
import ren.natsuyuk1.comet.util.toMessageWrapper
import ren.natsuyuk1.comet.utils.file.absPath

fun ProjectSekaiUserInfo.toMessageWrapper(): MessageWrapper =
    buildMessageWrapper {
        appendTextln("${user.userGameData.name} | ${user.userGameData.level} 级")
        appendLine()
        if (userProfile.bio.isNotBlank()) {
            appendTextln(userProfile.bio)
            appendLine()
        }
        appendTextln("歌曲游玩情况 >>")

        appendText(
            "EXPERT | Clear ${
            getSpecificLevelMusicCount(
                MusicDifficulty.EXPERT,
                MusicPlayResult.CLEAR
            )
            } / FC ${
            getSpecificLevelMusicCount(
                MusicDifficulty.EXPERT,
                MusicPlayResult.FULL_COMBO
            )
            } / AP ${getSpecificLevelMusicCount(MusicDifficulty.EXPERT, MusicPlayResult.ALL_PERFECT)}"
        )
        appendLine()
        appendText(
            "MASTER | Clear ${
            getSpecificLevelMusicCount(
                MusicDifficulty.MASTER,
                MusicPlayResult.CLEAR
            )
            } / FC ${
            getSpecificLevelMusicCount(
                MusicDifficulty.MASTER,
                MusicPlayResult.FULL_COMBO
            )
            } / AP ${getSpecificLevelMusicCount(MusicDifficulty.MASTER, MusicPlayResult.ALL_PERFECT)}"
        )
    }

/**
 * [ProjectSekaiUserInfo]
 *
 * 代表一个玩家的 `Project Sekai` 游戏数据.
 */
@Serializable
data class ProjectSekaiUserInfo(
    val user: UserGameData,
    val userProfile: UserProfile,
    // val userDecks
    // val userCards
    val userMusics: List<UserMusic>,
    val userMusicResults: List<MusicResult>
) {
    fun getBest30Songs(): List<MusicResult> = userMusicResults.filter {
        it.musicDifficulty >= MusicDifficulty.EXPERT && (it.isAllPerfect || it.isFullCombo)
    }.sortedBy {
        ProjectSekaiManager.getSongAdjustedLevel(it.musicId, it.musicDifficulty)
    }.asReversed()
        .distinctBy {
            it.musicId
        }.take(30)

    fun generateBest30(): MessageWrapper {
        if (ProjectSekaiManager.musicDiffDatabase.isEmpty() || ProjectSekaiManager.musicDatabase.isEmpty()) {
            return "Project Sekai 歌曲数据还没有加载好噢".toMessageWrapper()
        }

        val result = ProjectSekaiImageService.drawB30(user, getBest30Songs())

        return buildMessageWrapper {
            appendElement(Image(filePath = result.absPath))
        }
    }

    fun getSpecificLevelMusicCount(difficulty: MusicDifficulty, playResult: MusicPlayResult): Int {
        var counter = 0

        userMusics.forEach {
            counter += it.musicStatus.count { ms ->
                ms.userMusicResult.any { umr ->
                    umr.musicDifficulty == difficulty && umr.playResult >= playResult
                }
            }
        }

        return counter
    }

    @Serializable
    data class MusicResult(
        val userId: Long,
        val musicId: Int,
        val musicDifficulty: MusicDifficulty,
        val playType: String,
        val playResult: MusicPlayResult,
        val highScore: Int,
        @SerialName("fullComboFlg")
        val isFullCombo: Boolean,
        @SerialName("fullPerfectFlg")
        val isAllPerfect: Boolean,
        val mvpCount: Int,
        val superStarCount: Int
    )

    @Serializable
    data class UserGameData(
        @SerialName("userGamedata")
        val userGameData: Data
    ) {
        @Serializable
        data class Data(
            @SerialName("userId")
            val userID: Long,
            val name: String,
            @SerialName("rank")
            val level: Int
        )
    }

    @Serializable
    data class UserProfile(
        @SerialName("userId")
        val userID: Long,
        @SerialName("word")
        val bio: String,
        @SerialName("twitterId")
        val twitterID: String
    )

    @Serializable
    data class UserMusic(
        @SerialName("userId")
        val userID: Long,
        @SerialName("musicId")
        val musicID: Int,
        @SerialName("userMusicDifficultyStatuses")
        val musicStatus: List<MusicStatus>
    ) {
        @Serializable
        data class MusicStatus(
            @SerialName("musicId")
            val musicID: Int,
            val musicDifficulty: String,
            val musicDifficultyStatus: String,
            @SerialName("userMusicResults")
            val userMusicResult: List<UserMusicResult>
        ) {
            @Serializable
            data class UserMusicResult(
                // easy, normal, hard, expert, master
                val musicDifficulty: MusicDifficulty,
                // clear, full_combo, all_perfect
                val playResult: MusicPlayResult
            )
        }
    }
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
    MASTER
}

/**
 * [MusicPlayResult]
 *
 * 代表玩家游玩过曲目的状态.
 */
@Serializable
enum class MusicPlayResult {
    @SerialName("clear")
    CLEAR,

    @SerialName("full_combo")
    FULL_COMBO,

    @SerialName("full_perfect")
    ALL_PERFECT
}
