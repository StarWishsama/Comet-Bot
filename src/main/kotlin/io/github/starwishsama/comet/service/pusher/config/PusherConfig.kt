package io.github.starwishsama.comet.service.pusher.config

data class PusherConfig(
    /**
     * 用户名, 为B站推送使用
     */
    val userName: String?,
    val passWord: String?,
    val accessToken: String?,
    val secret: String?,
    var token: String?,
    /**
     * 推送间隔, 单位毫秒
     */
    val interval: Long,
    /**
     * 重置调用次数间隔, 单位毫秒
     */
    val resetInterval: Long,
    /**
     * 周期内最大可调用次数
     */
    val maxCallTime: Long
)