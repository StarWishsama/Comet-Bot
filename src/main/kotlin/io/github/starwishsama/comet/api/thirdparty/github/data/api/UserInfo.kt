package io.github.starwishsama.comet.api.thirdparty.github.data.api

import com.fasterxml.jackson.annotation.JsonProperty

data class UserInfo(
    val login: String,
    val id: Long,
    @JsonProperty("avatar_url")
    val avatarURL: String
)