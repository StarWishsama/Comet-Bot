package io.github.starwishsama.comet.service.pusher.config

open class PusherConfig(
    /**
     * 推送间隔, 单位毫秒
     */
    var interval: Long,
    /**
     * 缓存池
     */
    var cachePool: String = ""
)

class EmptyPusherConfig: PusherConfig(
    -1,
    ""
)