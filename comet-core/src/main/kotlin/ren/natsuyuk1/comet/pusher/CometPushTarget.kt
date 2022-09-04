package ren.natsuyuk1.comet.pusher

import kotlinx.serialization.Serializable
import ren.natsuyuk1.comet.api.platform.LoginPlatform

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
