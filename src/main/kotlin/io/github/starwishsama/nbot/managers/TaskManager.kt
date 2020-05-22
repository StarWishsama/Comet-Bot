package io.github.starwishsama.nbot.managers

import io.github.starwishsama.nbot.BotInstance
import java.util.concurrent.TimeUnit

object TaskManager {
    fun runAsync(task: () -> Unit, delay: Long) {
        BotInstance.service.schedule(task, delay, TimeUnit.SECONDS)
    }

    fun runScheduleTaskAsync(task: () -> Unit, delay: Long, period: Long, unit: TimeUnit) {
        BotInstance.service.scheduleAtFixedRate(task, delay, period, unit)
    }

    fun runScheduleTaskAsyncIf(task: () -> Unit, delay: Long, period: Long, unit: TimeUnit, condition: Boolean) {
        if (condition) {
            runScheduleTaskAsync(task, delay, period, unit)
        }
    }
}