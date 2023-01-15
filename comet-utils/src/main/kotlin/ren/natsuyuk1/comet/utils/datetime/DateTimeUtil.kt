/*
 * Copyright (c) 2019-2022 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 * Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 */

package ren.natsuyuk1.comet.utils.datetime

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.toJavaInstant
import ren.natsuyuk1.comet.utils.time.yyMMddPattern
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit
import kotlin.time.Duration
import kotlin.time.toKotlinDuration

fun Duration.toFriendly(maxUnit: TimeUnit = TimeUnit.SECONDS): String {
    toComponents { days, hours, minutes, seconds, ns ->
        return buildString {
            if (days != 0L && maxUnit <= TimeUnit.DAYS) {
                append("${days}天")
            }
            if (hours != 0 && maxUnit <= TimeUnit.HOURS) {
                append("${hours}时")
            }
            if (minutes != 0 && maxUnit <= TimeUnit.MINUTES) {
                append("${minutes}分")
            }
            if (seconds != 0 && maxUnit <= TimeUnit.SECONDS) {
                append("${seconds}秒")
            }
            if (ns != 0 && maxUnit <= TimeUnit.MILLISECONDS) {
                append("${ns / 1_000_000}毫秒")
            }
        }
    }
}

/**
 * 获取该 [LocalDateTime] 距今的时间
 */
fun LocalDateTime.getLastingTime(): Duration {
    val current = LocalDateTime.now()

    return java.time.Duration.between(this, current).toKotlinDuration()
}

/**
 * 获取该 [LocalDateTime] 距今的时间并转换为友好的字符串
 *
 * @param msMode 是否精准到毫秒
 */
fun LocalDateTime.getLastingTimeAsString(unit: TimeUnit = TimeUnit.SECONDS): String {
    val duration = getLastingTime()
    return duration.toFriendly(maxUnit = unit)
}

fun Instant.getLastingTimeAsString(unit: TimeUnit = TimeUnit.SECONDS): String {
    val duration = Clock.System.now() - this
    return duration.toFriendly(maxUnit = unit)
}

fun Instant.format(formatter: DateTimeFormatter = yyMMddPattern): String = formatter.format(toJavaInstant())

fun DayOfWeek.toChinese(): String =
    when (this) {
        DayOfWeek.MONDAY -> "星期一"
        DayOfWeek.TUESDAY -> "星期二"
        DayOfWeek.WEDNESDAY -> "星期三"
        DayOfWeek.THURSDAY -> "星期四"
        DayOfWeek.FRIDAY -> "星期五"
        DayOfWeek.SATURDAY -> "星期六"
        DayOfWeek.SUNDAY -> "星期日"
    }
