package io.github.starwishsama.comet.utils

import io.github.starwishsama.comet.BotVariables
import io.github.starwishsama.comet.exceptions.RateLimitException
import io.github.starwishsama.comet.exceptions.ReachRetryLimitException
import io.github.starwishsama.comet.utils.network.NetUtil
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

object TaskUtil {
    fun runAsync(task: () -> Unit, delay: Long, unit: TimeUnit = TimeUnit.SECONDS): ScheduledFuture<*> {
        return BotVariables.service.schedule(task, delay, unit)
    }

    fun runScheduleTaskAsync(task: () -> Unit, firstTimeDelay: Long, period: Long, unit: TimeUnit): ScheduledFuture<*> {
        return BotVariables.service.scheduleAtFixedRate(task, firstTimeDelay, period, unit)
    }

    fun executeWithRetry(task: () -> Unit, retryTime: Int): Throwable? {
        if (retryTime >= 5) return ReachRetryLimitException()

        var initRetryTime = 0
        fun runTask(): Throwable? {
            try {
                if (initRetryTime <= retryTime) {
                    task()
                }
            } catch (t: Throwable) {
                if (NetUtil.isTimeout(t)) {
                    initRetryTime++
                    BotVariables.daemonLogger.verbose("Retried failed, ${t.message}")
                    runTask()
                } else {
                    if (t !is RateLimitException) return t
                }
            }
            return null
        }

        return runTask()
    }
}