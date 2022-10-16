package ren.natsuyuk1.comet.pusher

import kotlinx.serialization.Serializable
import ren.natsuyuk1.comet.api.platform.LoginPlatform
import ren.natsuyuk1.comet.api.user.Group
import ren.natsuyuk1.comet.api.user.User

@Serializable
data class CometPushTarget(
    val id: Long,
    val type: CometPushTargetType,
    val platform: LoginPlatform,
)

enum class CometPushTargetType {
    USER,
    GROUP
}

fun Group.toCometPushTarget(): CometPushTarget = CometPushTarget(id, CometPushTargetType.GROUP, platform)

fun User.toCometPushTarget(): CometPushTarget = CometPushTarget(id, CometPushTargetType.USER, platform)
