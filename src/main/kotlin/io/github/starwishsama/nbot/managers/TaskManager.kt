package io.github.starwishsama.nbot.managers

import io.github.starwishsama.nbot.BotMain
import java.util.concurrent.TimeUnit

object TaskManager {
    fun runAsync(task: () -> Unit, delay: Long) {
        BotMain.service.schedule(task, delay, TimeUnit.SECONDS)
    }

    fun runScheduleTaskAsync(task: () -> Unit, delay: Long, period: Long, unit: TimeUnit) {
        BotMain.service.scheduleAtFixedRate(task, delay, period, unit)
    }

    fun runScheduleTaskAsyncIf(task: () -> Unit, delay: Long, period: Long, unit: TimeUnit, condition: Boolean) {
        if (condition) {
            runScheduleTaskAsync(task, delay, period, unit)
        }
    }
}