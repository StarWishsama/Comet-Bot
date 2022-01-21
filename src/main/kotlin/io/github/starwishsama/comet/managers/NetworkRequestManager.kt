/*
 * Copyright (c) 2019-2021 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package io.github.starwishsama.comet.managers

import io.github.starwishsama.comet.objects.tasks.network.INetworkRequestTask
import io.github.starwishsama.comet.objects.tasks.network.NetworkRequestTask
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

object NetworkRequestManager {
    private val requestQueue = ArrayDeque<NetworkRequestTask>()
    private val mutex = Mutex()

    suspend fun schedule() {
        if (requestQueue.isEmpty()) {
            return
        }

        if (mutex.isLocked) {
            return
        }

        val task = requestQueue.removeFirst()

        mutex.withLock {
            if (task is INetworkRequestTask<*>) {
                val result = task.request(task.param)
                task.callback(result)
            }
        }
    }

    fun addTask(task: NetworkRequestTask) {
        requestQueue.add(task)
    }
}