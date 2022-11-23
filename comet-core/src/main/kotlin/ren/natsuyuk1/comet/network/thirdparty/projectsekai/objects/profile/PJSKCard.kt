package ren.natsuyuk1.comet.network.thirdparty.projectsekai.objects.profile

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 表示一个 Project Sekai 的角色卡牌.
 * 由于仅需要资源文件, 暂时做了最小化处理.
 */
@Serializable
data class PJSKCard(
    val id: Int,
    val characterId: Int,
    @SerialName("assetbundleName")
    val assetBundleName: String,
)
