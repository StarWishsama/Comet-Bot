package ren.natsuyuk1.comet.network.thirdparty.projectsekai.objects

import kotlinx.serialization.Serializable

@Serializable
data class ProfileMusicInfo(
    val id: Int,
    val bpm: Float,
    val bpms: List<MusicBpmInfo>? = null,
    val duration: Double,
) {
    @Serializable
    data class MusicBpmInfo(
        val bar: Float,
        val bpm: Float,
        val duration: Double
    )
}
