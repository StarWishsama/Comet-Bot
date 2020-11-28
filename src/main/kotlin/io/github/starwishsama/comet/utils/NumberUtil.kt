package io.github.starwishsama.comet.utils

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

object NumberUtil {
    private val DECIMAL_FORMAT = DecimalFormat("#.##", DecimalFormatSymbols.getInstance(Locale.ROOT))

    /**
     * 获取更优雅的数字并转为字符串, 如 13200 -> 1.32w
     *
     * 扩展函数形式, 支持所有数字, 但可能会产生 [NumberFormatException]
     */
    @Throws(NumberFormatException::class)
    fun Number.getBetterNumber(): String = getCompactDouble(this.toDouble())

    /**
     * 获取更优雅的数字并转为字符串, 如 13200 -> 1.32w
     */
    private fun getCompactDouble(value: Double): String {
        if (value < 0) {
            // Negative numbers are a special case
            return '-'.toString() + getCompactDouble(-value)
        }
        return when {
            value < 1000.0 -> {
                // Below 1K
                DECIMAL_FORMAT.format(value)
            }
            value < 1000000.0 -> {
                // Thousands
                DECIMAL_FORMAT.format(value / 1000.0) + 'K'
            }
            value < 1000000000.0 -> {
                // Million
                DECIMAL_FORMAT.format(value / 1000000.0) + 'M'
            }
            value < 1000000000000.0 -> {
                // Billion
                DECIMAL_FORMAT.format(value / 1000000000.0) + 'B'
            }
            value < 1000000000000000.0 -> {
                // Trillion
                DECIMAL_FORMAT.format(value / 1000000000000.0) + 'T'
            }
            else -> {
                // Quadrillion
                DECIMAL_FORMAT.format(value / 1000000000000000.0) + 'Q'
            }
        }
    }

    /**
     * 时间戳转换为 [LocalDateTime]
     */
    fun Long.toLocalDateTime(): LocalDateTime =
            Instant.ofEpochMilli(this * 1000).atZone(ZoneId.systemDefault()).toLocalDateTime()
}