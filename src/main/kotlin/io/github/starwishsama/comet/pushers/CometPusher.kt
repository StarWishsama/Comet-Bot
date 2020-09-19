package io.github.starwishsama.comet.pushers

import java.util.concurrent.ScheduledFuture

interface CometPusher {
    /**
     * 首次获取前延迟的时间, 单位分钟
     */
    val delayTime: Long

    /**
     * 获取周期间隔时长, 单位分钟
     */
    val internal: Long

    /**
     * 获取该推送器的 Future
     */
    var future: ScheduledFuture<*>?

    /**
     * 获取逻辑
     */
    fun retrieve()

    /**
     * 推送逻辑
     */
    fun push()
}