package io.github.starwishsama.comet.logger

import java.util.*

object LoggerInstances {
    val instances = Collections.synchronizedSet(mutableSetOf<HinaLogger>())
}