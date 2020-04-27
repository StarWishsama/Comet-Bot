package io.github.starwishsama.nbot.managers

import io.github.starwishsama.nbot.BotInstance
import java.util.concurrent.TimeUnit

object TaskManager {
    fun runAsync(task: Runnable, delay: Long) {
        BotInstance.service.schedule(task, delay, TimeUnit.SECONDS)
    }

    fun runScheduleTaskAsync(task: Runnable, delay: Long, period: Long, unit: TimeUnit) {
        BotInstance.service.scheduleAtFixedRate(task, delay, period, unit)
    }
}