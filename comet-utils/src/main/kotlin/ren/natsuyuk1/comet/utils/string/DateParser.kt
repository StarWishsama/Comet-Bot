package ren.natsuyuk1.comet.utils.string

import kotlinx.datetime.DayOfWeek
import java.time.DayOfWeek.*

private val enFull = Regex("""^(mon|tues?|wed(nes)?|thurs?|fri|sat(ur)?|sun)(day|\.)?$""", RegexOption.IGNORE_CASE)

fun parseDayOfWeek(expr: String): DayOfWeek? {
    if (expr.isBlank()) return null
    val trim = expr.trim()

    trim.toIntOrNull()?.toDayOfWeek()?.let { return it }
    matchEn(trim)?.let { return it }
    matchZh(trim)?.let { return it }

    return null
}

private fun matchZh(expr: String): DayOfWeek? {
    if (expr.length == 1) {
        return expr.first().zhCharToWeek()
    }
    if (expr.length == 2 && expr.startsWith("周")) {
        expr[1].toString().toIntOrNull()?.toDayOfWeek()?.let { return it }
        expr[1].zhCharToWeek()?.let { return it }
    }
    if (expr.length == 3 && expr.startsWith("星期")) {
        expr[2].toString().toIntOrNull()?.toDayOfWeek()?.let { return it }
        expr[2].zhCharToWeek()?.let { return it }
    }
    return null
}

private fun matchEn(expr: String): DayOfWeek? {
    val full = enFull.find(expr) ?: return null
    val g1 = full.groups[1]?.value?.lowercase() ?: return null
    return when {
        g1 == "mon" -> DayOfWeek.MONDAY
        g1.startsWith("tue") -> DayOfWeek.TUESDAY
        g1.startsWith("wed") -> DayOfWeek.WEDNESDAY
        g1.startsWith("thur") -> DayOfWeek.THURSDAY
        g1 == "fri" -> DayOfWeek.FRIDAY
        g1.startsWith("sat") -> DayOfWeek.SATURDAY
        g1 == "sun" -> DayOfWeek.SUNDAY
        else -> null
    }
}

private fun Int.toDayOfWeek(): DayOfWeek? = when (this) {
    1 -> MONDAY
    2 -> TUESDAY
    3 -> WEDNESDAY
    4 -> THURSDAY
    5 -> FRIDAY
    6 -> SATURDAY
    7 -> SUNDAY
    else -> null
}

private fun Char.zhCharToWeek(): DayOfWeek? = when (this) {
    '一' -> MONDAY
    '二' -> TUESDAY
    '三' -> WEDNESDAY
    '四' -> THURSDAY
    '五' -> FRIDAY
    '六' -> SATURDAY
    '日' -> SUNDAY
    '七' -> SUNDAY
    else -> null
}
