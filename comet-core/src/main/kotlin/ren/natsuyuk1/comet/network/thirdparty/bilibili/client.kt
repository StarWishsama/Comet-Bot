package ren.natsuyuk1.comet.network.thirdparty.bilibili

import kotlinx.serialization.json.Json
import moe.sdl.yabapi.BiliClient
import moe.sdl.yabapi.Yabapi
import moe.sdl.yabapi.Yabapi.log
import moe.sdl.yabapi.enums.LogLevel
import mu.KotlinLogging

private var isInitializedYabapi = false
private val logger = KotlinLogging.logger {}

val client = BiliClient()

fun initYabapi() {
    Yabapi.apply {
        defaultJson.lazySet(
            Json {
                prettyPrint = true
                isLenient = true
                coerceInputValues = true
                ignoreUnknownKeys = true
            }
        )
    }

    if (!isInitializedYabapi) {
        log.getAndSet { tag: String, level: LogLevel, throwable: Throwable?, message: () -> String ->
            when (level) {
                LogLevel.VERBOSE -> logger.trace("$tag ${message()}", throwable)
                LogLevel.DEBUG -> logger.debug("$tag ${message()}", throwable)
                LogLevel.INFO -> logger.info("$tag ${message()}", throwable)
                LogLevel.WARN -> logger.warn("$tag ${message()}", throwable)
                LogLevel.ERROR -> logger.error("$tag ${message()}", throwable)
                LogLevel.ASSERT -> logger.error("----- ASSERT ERROR ----- $tag ${message()}", throwable)
            }
        }

        isInitializedYabapi = true
    }
}
