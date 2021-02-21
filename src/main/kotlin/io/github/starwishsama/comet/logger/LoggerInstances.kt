package io.github.starwishsama.comet.logger

import java.util.*

object LoggerInstances {
    val instances: MutableSet<HinaLogger> = Collections.synchronizedSet(mutableSetOf<HinaLogger>())
}