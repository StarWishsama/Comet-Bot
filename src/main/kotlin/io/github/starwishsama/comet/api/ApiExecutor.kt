package io.github.starwishsama.comet.api

/**
 * Api 调用器
 * 摒除使用多个乱七八糟的 Util 类
 * 便于统计调用次数以防止超过部分 API 调用上限
 * @author Nameless
 */
interface ApiExecutor {
    var usedTime: Int
    fun isReachLimit(): Boolean {
        return usedTime > getLimitTime()
    }

    fun getLimitTime(): Int
    fun resetTime() {
        usedTime = 0
    }
}