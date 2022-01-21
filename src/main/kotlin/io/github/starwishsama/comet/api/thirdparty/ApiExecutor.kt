/*
 * Copyright (c) 2019-2021 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package io.github.starwishsama.comet.api.thirdparty

import io.github.starwishsama.comet.exceptions.RateLimitException

/**
 * Api 调用器
 * 摒除使用多个乱七八糟的 Util 类
 * 便于统计调用次数以防止超过部分 API 调用上限
 * @author StarWishsama
 */
interface ApiExecutor {
    var usedTime: Int
    val duration: Int

    fun isReachLimit(): Boolean {
        return usedTime > getLimitTime()
    }

    fun getLimitTime(): Int

    fun resetTime() {
        usedTime = 0
    }

    fun checkRateLimit(message: String = "") {
        if (isReachLimit()) throw RateLimitException(message)
    }
}