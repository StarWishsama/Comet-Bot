package io.github.starwishsama.comet.service.pusher.config

import io.github.starwishsama.comet.service.pusher.context.PushContext

open class PusherConfig(
    /**
     * 推送间隔, 单位毫秒
     */
    var interval: Long,
    /**
     * 缓存池
     */
    val cachePool: MutableList<PushContext>
)

class EmptyPusherConfig: PusherConfig(
    -1,
    mutableListOf()
)