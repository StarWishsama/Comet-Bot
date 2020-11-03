package io.github.starwishsama.comet.utils

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

object NumberUtil {
    /**
     * 获取更优雅的数字, 如 13200 -> 1.32w
     */
    fun Long.getBetterNumber(): String {
        return when {
            this >= 1000 -> div(1000.0).toString() + "k"
            this >= 10000 -> div(10000.0).toString() + "w"
            else -> toString()
        }
    }

    /**
     * 时间戳转换为 [LocalDateTime]
     */
    fun Long.toLocalDateTime(): LocalDateTime =
            Instant.ofEpochMilli(this * 1000).atZone(ZoneId.systemDefault()).toLocalDateTime()
}