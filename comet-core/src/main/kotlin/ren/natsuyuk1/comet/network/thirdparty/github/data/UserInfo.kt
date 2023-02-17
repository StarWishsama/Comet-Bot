package ren.natsuyuk1.comet.network.thirdparty.github.data

import kotlinx.serialization.SerialName

data class UserInfo(
    val login: String,
    val id: Long,
    @SerialName("avatar_url")
    val avatarURL: String,
)
