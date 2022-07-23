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
        appendText("歌曲信息 >", true)
        val clear = userMusic.count { it.musicStatus.firstOrNull()?.userMusicResult?.firstOrNull() != null }
        val fc =
            userMusic.count { it.musicStatus.firstOrNull()?.userMusicResult?.firstOrNull()?.playResult == "full_combo" }
        val ap =
            userMusic.count { it.musicStatus.firstOrNull()?.userMusicResult?.firstOrNull()?.playResult == "all_perfect" }
        appendText("Clear $clear / FC $fc / AP $ap")
    }

@kotlinx.serialization.Serializable
data class ProjectSekaiUserInfo(
    val user: UserGameData,
    val userProfile: UserProfile,
    //val userDecks
    //val userCards
    val userMusic: List<UserMusic>
) {
    @kotlinx.serialization.Serializable
    data class UserGameData(
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
                val musicDifficulty: String,
                // clear, full_combo, all_perfect
                val playResult: String,
            )
        }
    }
}
