package ren.natsuyuk1.comet.network.server.request

import kotlinx.serialization.Serializable
import ren.natsuyuk1.comet.api.platform.LoginPlatform
import ren.natsuyuk1.comet.api.platform.MiraiLoginProtocol

@Serializable
data class CometLoginReq(
    val id: Long,
    val password: String,
    val platform: LoginPlatform,
    val miraiLoginProtocol: MiraiLoginProtocol? = null,
)
