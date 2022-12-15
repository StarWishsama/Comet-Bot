package ren.natsuyuk1.comet.network.thirdparty.projectsekai.objects.official

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
)
