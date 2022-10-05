package ren.natsuyuk1.comet.utils.time

import java.time.ZoneId
import java.time.format.DateTimeFormatter

val hourMinutePattern: DateTimeFormatter by lazy {
    DateTimeFormatter.ofPattern("HH:mm").withZone(ZoneId.systemDefault())
}

val hmsPattern: DateTimeFormatter by lazy {
    DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZoneId.systemDefault())
}

val yyMMddWithTimePattern: DateTimeFormatter by lazy {
    DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss").withZone(ZoneId.systemDefault())
}

val yyMMddWithTimeZonePattern: DateTimeFormatter by lazy {
    DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss Z").withZone(ZoneId.systemDefault())
}

val yyMMddPattern: DateTimeFormatter by lazy {
    DateTimeFormatter.ofPattern("yyyy/MM/dd").withZone(ZoneId.systemDefault())
}
