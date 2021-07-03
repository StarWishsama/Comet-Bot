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

    fun Number.formatDigests(digest: Int = 1): String {
        if (this is Double) {
            return String.format("%.${digest}f", this)
        }
        return this.toString()
    }

    /**
     * 获取更优雅的数字并转为字符串, 如 13200 -> 1.32w
     */
    private fun getCompactDouble(value: Double): String {
        if (value < 0) {
            // 处理负数
            return '-'.toString() + getCompactDouble(-value)
        }

        return when {
            value < 1000.0 -> {
                // 小于一千
                DECIMAL_FORMAT.format(value)
            }
            value < 1000000.0 -> {
                // 千
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
     * 时间戳 (秒) 转换为 [LocalDateTime]
     */
    fun Long.toLocalDateTime(isMillis: Boolean = false): LocalDateTime =
        Instant.ofEpochMilli(if (isMillis) this else this * 1000).atZone(ZoneId.systemDefault()).toLocalDateTime()
}