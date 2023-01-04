package ren.natsuyuk1.comet.network.thirdparty.projectsekai.objects

import kotlinx.serialization.Serializable

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
)
