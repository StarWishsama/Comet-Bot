package io.github.starwishsama.comet.logger

import io.github.starwishsama.comet.CometVariables
import moe.sdl.yabapi.Yabapi
import moe.sdl.yabapi.enums.LogLevel

object YabapiLogRedirecter {
    private val loggerWrapper = CometVariables.daemonLogger
    private var isInitializedYabapi = false

    internal fun initYabapi() = Yabapi.apply {
        if (!isInitializedYabapi) {
            log.getAndSet { tag: String, level: LogLevel, throwable: Throwable?, message: () -> String ->
                when (level) {
                    LogLevel.VERBOSE -> loggerWrapper.verbose("$tag ${message()}", throwable)
                    LogLevel.DEBUG -> loggerWrapper.debug("$tag ${message()}", throwable)
                    LogLevel.INFO -> loggerWrapper.info("$tag ${message()}", throwable)
                    LogLevel.WARN -> loggerWrapper.warning("$tag ${message()}", throwable)
                    LogLevel.ERROR -> loggerWrapper.error("$tag ${message()}", throwable)
                    LogLevel.ASSERT -> loggerWrapper.error("----- ASSERT ERROR ----- $tag ${message()}", throwable)
                }
            }

            isInitializedYabapi = true
        }
    }
}