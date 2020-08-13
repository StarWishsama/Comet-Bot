package io.github.starwishsama.comet.utils

import io.github.starwishsama.comet.BotVariables
import org.apache.commons.lang3.Validate
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

object TaskUtil {
    fun runAsync(task: () -> Unit, delay: Long, unit: TimeUnit = TimeUnit.SECONDS): ScheduledFuture<*> {
        return BotVariables.service.schedule(task, delay, unit)
    }

    fun runScheduleTaskAsync(task: () -> Unit, firstTimeDelay: Long, period: Long, unit: TimeUnit): ScheduledFuture<*> {
        return BotVariables.service.scheduleAtFixedRate(task, firstTimeDelay, period, unit)
    }

    fun runScheduleTaskAsyncIf(
            task: () -> Unit,
            delay: Long,
            period: Long,
            unit: TimeUnit,
            condition: Boolean
    ): ScheduledFuture<*> {
        Validate.isTrue(condition)
        return runScheduleTaskAsync(
            task,
            delay,
            period,
            unit
        )
    }
}