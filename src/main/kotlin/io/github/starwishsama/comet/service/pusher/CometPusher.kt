package io.github.starwishsama.comet.service.pusher

import io.github.starwishsama.comet.exceptions.RateLimitException
import io.github.starwishsama.comet.service.pusher.config.PusherConfig
import io.github.starwishsama.comet.utils.TaskUtil
import java.util.concurrent.TimeUnit

/**
 * [CometPusher]
 */
abstract class CometPusher(val name: String, val config: PusherConfig) {
    abstract val cachePool: MutableList<PushContext<*>>

    val duration: Long = config.interval

    var callTime: Int = 0

    var pushTime: Int = 0

    open fun isReachApiLimit(): Boolean {
        return callTime > config.maxCallTime
    }

    abstract fun retrieve()

    abstract fun push()

    fun start() {
        TaskUtil.runScheduleTaskAsync(duration, duration, TimeUnit.SECONDS) {
            retrieve()
            push()
        }
    }

    fun checkReachLimit() {
        if (!isReachApiLimit()) {
            throw RateLimitException("$name 已达到 API 调用次数上限")
        }
    }

    fun addPushTime(){
        pushTime += 1
    }

    fun resetPushTime() {
        pushTime = 0
    }
}