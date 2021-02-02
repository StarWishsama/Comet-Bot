package io.github.starwishsama.comet.service.pusher.config

data class PusherConfig(
    val userName: String?,
    val passWord: String?,
    val accessToken: String?,
    val secret: String?,
    var token: String?,
    val interval: Long,
    val maxCallTime: Long
)