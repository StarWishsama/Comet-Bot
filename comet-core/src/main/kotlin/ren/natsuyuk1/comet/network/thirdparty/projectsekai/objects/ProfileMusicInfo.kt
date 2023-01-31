package ren.natsuyuk1.comet.network.thirdparty.projectsekai.objects

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import ren.natsuyuk1.comet.network.thirdparty.projectsekai.MusicID

@Serializable
data class ProfileMusicInfo(
    val id: MusicID,
    val title: String,
    val pronunciation: String,
    val lyricist: String,
    val composer: String,
    val arranger: String,
    @SerialName("assetbundleName")
    val assetBundleName: String,
    val publishedAt: Long,
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
