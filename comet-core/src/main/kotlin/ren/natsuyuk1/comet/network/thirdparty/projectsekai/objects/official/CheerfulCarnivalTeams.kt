package ren.natsuyuk1.comet.network.thirdparty.projectsekai.objects.official

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CheerfulCarnivalTeams(
    val teams: List<CheerfulCarnivalTeam>
) {
    @Serializable
    data class CheerfulCarnivalTeam(
        val id: Int,
        val eventId: Int,
        @SerialName("seq")
        val sequence: Int
    )
}
