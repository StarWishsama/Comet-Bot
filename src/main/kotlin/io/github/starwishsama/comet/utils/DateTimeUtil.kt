package io.github.starwishsama.comet.utils

import java.time.LocalDateTime

internal val gaokaoDateTime = run {
    val now = LocalDateTime.now()
    val thisYear = LocalDateTime.of(now.year, 6, 7, 0, 0)
    val nextYear = LocalDateTime.of(now.year + 1, 6, 7, 0, 0)
    if (now > thisYear) nextYear else thisYear
}
