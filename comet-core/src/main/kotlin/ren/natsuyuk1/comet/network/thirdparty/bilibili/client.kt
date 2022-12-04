package ren.natsuyuk1.comet.network.thirdparty.bilibili

import io.ktor.client.plugins.cookies.*
import kotlinx.serialization.json.Json
import moe.sdl.yabapi.BiliClient
import moe.sdl.yabapi.Yabapi
import moe.sdl.yabapi.Yabapi.log
import moe.sdl.yabapi.consts.getDefaultHttpClient
import moe.sdl.yabapi.enums.LogLevel
import moe.sdl.yabapi.storage.FileCookieStorage
import moe.sdl.yabapi.util.string.cookieFromHeader
import mu.KotlinLogging
import ren.natsuyuk1.comet.api.config.CometGlobalConfig
import ren.natsuyuk1.comet.utils.file.configDirectory

private var isInitializedYabapi = false
private val logger = KotlinLogging.logger {}

lateinit var biliClient: BiliClient

suspend fun initYabapi() {
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

        biliClient = BiliClient(
            getDefaultHttpClient(
                FileCookieStorage(configDirectory.resolve("bili_token")).apply {
                    if (CometGlobalConfig.data.biliCookie.isEmpty()) {
                        return@apply
                    }

                    cookieFromHeader(CometGlobalConfig.data.biliCookie).forEach {
                        addCookie("https://.bilibili.com", it)
                    }
                }
            )
        )

        isInitializedYabapi = true
    }
}
