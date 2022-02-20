/*
 * Copyright (c) 2019-2021 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package io.github.starwishsama.comet.utils

import cn.hutool.core.thread.ThreadFactoryBuilder
import io.github.starwishsama.comet.CometVariables.daemonLogger
import io.github.starwishsama.comet.exceptions.ApiException
import io.github.starwishsama.comet.exceptions.ReachRetryLimitException
import io.github.starwishsama.comet.utils.network.NetUtil
import kotlinx.coroutines.CancellationException
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit

object TaskUtil {
    val service = ScheduledThreadPoolExecutor(
        10,
        ThreadFactoryBuilder.create()
            .setNamePrefix("comet-service-%d")
            .setUncaughtExceptionHandler { t, e ->
                daemonLogger.warning("线程 ${t.name} 在执行任务时发生了错误", e)
            }.build()
    )

    fun schedule(delay: Long = 0, unit: TimeUnit = TimeUnit.SECONDS, task: () -> Unit): ScheduledFuture<*> {
        return service.schedule({
            try {
                task()
            } catch (e: Throwable) {
                daemonLogger.warning("执行任务时遇到了意外", e)
            }
        }, delay, unit)
    }

    fun scheduleAtFixedRate(firstTimeDelay: Long, period: Long, unit: TimeUnit, task: () -> Unit): ScheduledFuture<*> {
        return service.scheduleAtFixedRate({
            try {
                task()
            } catch (e: Throwable) {
                daemonLogger.warning("执行任务时遇到了意外", e)
            }
        }, firstTimeDelay, period, unit)
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
            } catch (e: Exception) {
                if (NetUtil.isTimeout(e)) {
                    daemonLogger.info("Retried $it time(s), connect times out")
                    return@repeat
                } else if (e !is ApiException) {
                    if (e is CancellationException) {
                        throw e
                    }
                    return e
                }
            }
        }

        return null
    }
}