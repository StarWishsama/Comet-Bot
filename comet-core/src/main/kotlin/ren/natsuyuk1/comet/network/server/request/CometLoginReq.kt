package ren.natsuyuk1.comet.network.server.request

import kotlinx.serialization.Serializable
import ren.natsuyuk1.comet.api.platform.CometPlatform
import ren.natsuyuk1.comet.api.platform.MiraiLoginProtocol

@Serializable
data class CometLoginReq(
    val id: Long,
    val password: String,
    val platform: CometPlatform,
    val miraiLoginProtocol: MiraiLoginProtocol? = null,
)
