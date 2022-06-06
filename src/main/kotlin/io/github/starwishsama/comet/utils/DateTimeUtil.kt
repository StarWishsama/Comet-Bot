package io.github.starwishsama.comet.utils

import java.time.LocalDateTime
import java.time.ZoneId

internal val gaokaoDateTime = run {
    val now = LocalDateTime.now().atZone(ZoneId.of("UTC"))
    val thisYear = LocalDateTime.of(now.year, 6, 7, 0, 0).atZone(ZoneId.of("UTC"))
    val nextYear = LocalDateTime.of(now.year + 1, 6, 7, 0, 0).atZone(ZoneId.of("UTC"))
    if (now > thisYear) nextYear else thisYear
}
