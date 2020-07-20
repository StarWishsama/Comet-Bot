package io.github.starwishsama.comet.managers

import io.github.starwishsama.comet.BotVariables
import java.util.concurrent.TimeUnit

object TaskManager {
    fun runAsync(task: () -> Unit, delay: Long) {
        BotVariables.service.schedule(task, delay, TimeUnit.SECONDS)
    }

    fun runScheduleTaskAsync(task: () -> Unit, firstTimeDelay: Long, period: Long, unit: TimeUnit) {
        BotVariables.service.scheduleAtFixedRate(task, firstTimeDelay, period, unit)
    }

    fun runScheduleTaskAsyncIf(task: () -> Unit, delay: Long, period: Long, unit: TimeUnit, condition: Boolean) {
        if (condition) {
            runScheduleTaskAsync(task, delay, period, unit)
        }
    }
}