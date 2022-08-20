package ren.natsuyuk1.comet.network.thirdparty.bangumi.data.resp

import kotlinx.serialization.Serializable

@Serializable
data class FollowStatus(
    val num: String,
    val link: String,
    val type: FollowType
)

@Serializable
enum class FollowType {
    WISH, WATCHED, WATCHING, PENDING, DROPPED;

    companion object {
        fun parse(str: String) = when {
            str.contains("想看") -> WISH
            str.contains("看过") -> WATCHED
            str.contains("在看") -> WATCHING
            str.contains("搁置") -> PENDING
            str.contains("抛弃") -> DROPPED
            else -> null
        }
    }
}
