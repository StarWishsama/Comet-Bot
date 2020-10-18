package io.github.starwishsama.comet.utils

import io.github.starwishsama.comet.BotVariables
import io.github.starwishsama.comet.BotVariables.daemonLogger
import io.github.starwishsama.comet.exceptions.RateLimitException
import io.github.starwishsama.comet.exceptions.ReachRetryLimitException
import io.github.starwishsama.comet.utils.network.NetUtil
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

object TaskUtil {
    fun runAsync(delay: Long = 0, unit: TimeUnit = TimeUnit.SECONDS, task: () -> Unit): ScheduledFuture<*> {
        return BotVariables.service.schedule(task, delay, unit)
    }

    fun runScheduleTaskAsync(firstTimeDelay: Long, period: Long, unit: TimeUnit, task: () -> Unit): ScheduledFuture<*> {
        return BotVariables.service.scheduleAtFixedRate(task, firstTimeDelay, period, unit)
    }

    fun executeWithRetry(retryTime: Int, task: () -> Unit): Throwable? {
        if (retryTime >= 5) return ReachRetryLimitException()

        repeat(retryTime) {
            try {
                if (it <= retryTime) {
                    task()
                } else {
                    throw ReachRetryLimitException()
                }
            } catch (e: RuntimeException) {
                if (NetUtil.isTimeout(e)) {
                    daemonLogger.verbose("Retried $it time(s), connect times out")
                    return@repeat
                } else {
                    if (e !is RateLimitException) return e
                }
            }
        }

        return null
    }
}