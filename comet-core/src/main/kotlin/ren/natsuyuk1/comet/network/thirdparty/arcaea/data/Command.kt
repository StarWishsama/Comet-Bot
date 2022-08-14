package ren.natsuyuk1.comet.network.thirdparty.arcaea.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Command(
    @SerialName("cmd")
    val command: ArcaeaCommand
)

@Serializable
enum class ArcaeaCommand {
    @SerialName("userinfo")
    USER_INFO,

    @SerialName("scores")
    SCORES,

    @SerialName("songtitle")
    SONG_TITLE,

    @SerialName("songartist")
    SONG_ARTIST,
}
