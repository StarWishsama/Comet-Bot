package io.github.starwishsama.comet.utils

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

object NumberUtil {
    fun Long.getBetterNumber(): String {
        return when {
            this >= 1000 -> div(1000.0).toString() + "k"
            this >= 10000 -> div(10000.0).toString() + "w"
            else -> toString()
        }
    }

    fun Long.toLocalDateTime(): LocalDateTime =
        Instant.ofEpochMilli(this * 1000).atZone(ZoneId.systemDefault()).toLocalDateTime()
}