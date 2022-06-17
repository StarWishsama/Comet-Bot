/*
 * Copyright (c) 2019-2022 StarWishsama.
 *
 * 此源代码的使用受 MIT 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 * Use of this source code is governed by the MIT License which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/dev/LICENSE
 */

package ren.natsuyuk1.comet.network.thirdparty.projectsekai.objects

@kotlinx.serialization.Serializable
data class ProjectSekaiEventList(
    val total: Int,
    val limit: Int,
    val skip: Int,
    val data: List<EventData>
) {
    @kotlinx.serialization.Serializable
    data class EventData(
        val id: Int,
        // 已知的有 marathon (普通玩法), cheerful_carnival (选边玩法)
        val eventType: String,
        val name: String,
        // 开始时间
        val startAt: Long,
        // 结算时间
        val aggregateAt: Long,
        // 结算结果公布时间
        val rankingAnnounceAt: Long,
        // 奖励分发时间 (?)
        val distributionStartAt: Long,
        // 结束时间
        val closedAt: Long,
        // 关店时间
        val distributionEndAt: Long
    )
}
