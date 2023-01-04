package ren.natsuyuk1.comet.network.thirdparty.projectsekai.objects

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PJSKMusicInfo(
    val id: Int,
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
