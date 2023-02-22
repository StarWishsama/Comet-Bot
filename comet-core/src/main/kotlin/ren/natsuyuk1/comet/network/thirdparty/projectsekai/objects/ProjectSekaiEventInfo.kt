package ren.natsuyuk1.comet.network.thirdparty.projectsekai.objects

import kotlinx.serialization.Serializable

@Serializable
data class ProjectSekaiEventInfo(
    val rankings: List<UserEventInfo>,
) {
    @Serializable
    data class UserEventInfo(
        val isOwn: Boolean,
        val name: String,
        val rank: Int,
        val score: Long,
        val userCard: ProjectSekaiUserInfo.UserCard,
        val userCheerfulCarnival: UserCheerfulCarnival, // may change in 3.5.0, need to be updated
        val userId: Long,
        val userProfile: ProjectSekaiUserInfo.UserProfile,
        val userProfileHonors: List<ProjectSekaiUserInfo.UserHonor>,
    )

    @Serializable
    data class UserCheerfulCarnival(
        val eventId: Int? = null,
        val cheerfulCarnivalTeamId: Int? = null,
        val teamChangeCount: Int? = null,
        val registerAt: Long? = null,
    )
}
