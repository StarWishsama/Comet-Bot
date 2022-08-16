package ren.natsuyuk1.comet.util

import java.time.DateTimeException
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.zone.ZoneRulesException
import java.util.*

fun newFormatterOrNull(pattern: String): DateTimeFormatter? =
    try {
        DateTimeFormatter.ofPattern(pattern)
    }  catch (e: IllegalArgumentException) {
        null
    }

fun newZoneIdOrNull(id: String): ZoneId? =
    try {
        ZoneId.of(id)
    } catch (e: DateTimeException) {
        null
    } catch (e: ZoneRulesException) {
        null
    }

fun newTimeZoneOrNull(zone: String): TimeZone? {
    newZoneIdOrNull(zone)?.let {
        return TimeZone.getTimeZone(it)
    }

    val tz = TimeZone.getTimeZone(zone)

    if (tz.id == "GMT") return null

    return tz
}
