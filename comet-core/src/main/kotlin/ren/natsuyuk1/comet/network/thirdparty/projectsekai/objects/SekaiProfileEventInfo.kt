/*
 * Copyright (c) 2019-2022 StarWishsama.
 *
 * 此源代码的使用受 MIT 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 * Use of this source code is governed by the MIT License which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/dev/LICENSE
 */

package ren.natsuyuk1.comet.network.thirdparty.projectsekai.objects

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SekaiProfileEventInfo(
    val rankings: List<EventInfo>
) {
    fun getScore(): Long = rankings.first().score

    @Serializable
    data class EventInfo(
        val userId: Long,
        val score: Long,
        val rank: Int,
        val isOwn: Boolean,
        val name: String,
        val userCard: UserCard,
        val userProfile: UserProfile,
        val userCheerfulCarnival: UserCheerfulCarnival?
    ) {
        @Serializable
        data class UserCard(
            val cardId: String,
            val level: Int,
            val masterRank: Int,
            val specialTrainingStatus: String,
            val defaultImage: String
        )

        @Serializable
        data class UserProfile(
            val userId: Long,
            @SerialName("word")
            val bio: String,
            val twitterId: String,
            val profileImageType: String
        )

        @Serializable
        data class UserCheerfulCarnival(
            val eventId: Int,
            val cheerfulCarnivalTeamId: Int,
            val teamChangeCount: Int,
            val registerAt: Long
        )
    }
}
