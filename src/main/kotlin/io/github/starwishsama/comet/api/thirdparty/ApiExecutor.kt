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