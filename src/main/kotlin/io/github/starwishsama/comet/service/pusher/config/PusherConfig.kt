/*
 * Copyright (c) 2019-2021 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package io.github.starwishsama.comet.service.pusher.config

import io.github.starwishsama.comet.service.pusher.context.PushContext
import java.util.concurrent.TimeUnit

open class PusherConfig(
    /**
     * 推送间隔, 单位由 [timeUnit] 决定
     */
    val interval: Long,

    /**
     * 时间单位
     */
    val timeUnit: TimeUnit = TimeUnit.MINUTES,

    /**
     * 缓存池
     */
    val cachePool: MutableList<PushContext> = mutableListOf()
)