package ren.natsuyuk1.comet.network.thirdparty.projectsekai.objects

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import ren.natsuyuk1.comet.network.thirdparty.projectsekai.MusicID

@Serializable
data class ProjectSekaiMusicInfo(
    val id: MusicID,
    val title: String,
    val pronunciation: String,
    val lyricist: String,
    val composer: String,
    val arranger: String,
    @SerialName("assetbundleName")
    val assetBundleName: String,
    val publishedAt: Long,
)
