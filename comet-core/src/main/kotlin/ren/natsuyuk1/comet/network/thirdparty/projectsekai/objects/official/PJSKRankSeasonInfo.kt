package ren.natsuyuk1.comet.network.thirdparty.projectsekai.objects.official

import kotlinx.serialization.Serializable
import ren.natsuyuk1.comet.network.thirdparty.projectsekai.objects.SekaiEventStatus

@Serializable
data class PJSKRankSeasonInfo(
    val id: Int,
    val name: String,
    val startAt: Long,
    val aggregatedAt: Long,
    val rankingPublishedAt: Long,
    val batchExecutionAt: Long,
    val distributionStartAt: Long,
    val distributionEndAt: Long,
    val closedAt: Long,
    val isDisplayResult: Boolean
) {
    fun getStatus(time: Long = System.currentTimeMillis()): SekaiEventStatus {
        return when (time) {
            in startAt..aggregatedAt -> {
                SekaiEventStatus.ONGOING
            }

            in aggregatedAt..rankingPublishedAt -> {
                SekaiEventStatus.COUNTING
            }

            else -> {
                SekaiEventStatus.END
            }
        }
    }
}
