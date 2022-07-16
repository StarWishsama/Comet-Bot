package ren.natsuyuk1.comet.utils.time

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.Duration

class Timer {
    private val startTime: Instant = Clock.System.now()

    fun measureDuration(): Duration = Clock.System.now() - startTime
}
