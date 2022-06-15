/*
 * Copyright (c) 2019-2022 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 * Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 */

package ren.natsuyuk1.comet.network.thirdparty.projectsekai.objects

import kotlinx.serialization.SerialName

@kotlinx.serialization.Serializable
data class ProjectSekaiProfile(
    val rankings: List<EventInfo>
) {
    @kotlinx.serialization.Serializable
    data class EventInfo(
        val userId: ULong,
        val score: ULong,
        val rank: UInt,
        val isOwn: Boolean,
        val name: String,
        val userCard: UserCard,
        val userProfile: UserProfile,
        val userProfileHonors: List<UserProfileHonor>
    ) {
        @kotlinx.serialization.Serializable
        data class UserCard(
            val cardId: String,
            val level: UInt,
            val masterRank: UInt,
            val specialTrainingStatus: String,
            val defaultImage: String
        )

        @kotlinx.serialization.Serializable
        data class UserProfile(
            val userId: ULong,
            @SerialName("word")
            val bio: String,
            val twitterId: String,
            val profileImageType: String
        )

        @kotlinx.serialization.Serializable
        data class UserProfileHonor(
            val seq: UInt,
            val profileHonorType: String,
            val honorId: UInt,
            val honorLevel: UInt,
            val bondsHonorViewType: String,
            val bondsHonorWordId: UInt
        )
    }
}
