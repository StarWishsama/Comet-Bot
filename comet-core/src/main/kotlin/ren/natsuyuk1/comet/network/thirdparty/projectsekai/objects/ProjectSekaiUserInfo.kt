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

        val clear = getSpecificMusicCount(MusicDifficulty.CLEAR)
        val fc = getSpecificMusicCount(MusicDifficulty.FULL_COMBO)
        val ap = getSpecificMusicCount(MusicDifficulty.ALL_PERFECT)
        appendText("Clear $clear / FC $fc / AP $ap")
    }

@kotlinx.serialization.Serializable
data class ProjectSekaiUserInfo(
    val user: UserGameData,
    val userProfile: UserProfile,
    //val userDecks
    //val userCards
    val userMusics: List<UserMusic>
) {
    fun getSpecificMusicCount(playResult: MusicDifficulty): Int {
        var counter = 0

        userMusics.forEach {
            it.musicStatus.forEach ms@{ ms ->
                if (ms.userMusicResult.any { umr -> umr.playResult == playResult }) {
                    counter += 1
                }
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
                val musicDifficulty: String,
                // clear, full_combo, all_perfect
                val playResult: MusicDifficulty,
            )
        }
    }
}


@kotlinx.serialization.Serializable
enum class MusicDifficulty {
    @SerialName("clear")
    CLEAR,

    @SerialName("full_combo")
    FULL_COMBO,

    @SerialName("all_perfect")
    ALL_PERFECT
}
