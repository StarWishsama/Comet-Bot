/*
 * Copyright (c) 2019-2022 StarWishsama.
 *
 * 此源代码的使用受 MIT 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 * Use of this source code is governed by the MIT License which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/dev/LICENSE
 */

package ren.natsuyuk1.comet.consts

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.compression.*
import io.ktor.client.plugins.cookies.*
import io.ktor.client.plugins.logging.*
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import ren.natsuyuk1.comet.api.config.CometGlobalConfig
import ren.natsuyuk1.comet.network.CometClient
import ren.natsuyuk1.comet.utils.ktor.initProxy
import ren.natsuyuk1.comet.utils.time.Timer

val json = Json {
    prettyPrint = true
    isLenient = true
    ignoreUnknownKeys = true
}

val defaultClient = HttpClient(CIO) {
    engine {
        requestTimeout = 0
        initProxy()
    }

    install(UserAgent) {
        agent =
            CometGlobalConfig.data.useragent
    }

    install(ContentEncoding) {
        gzip()
        deflate()
        identity()
    }

    install(HttpCookies)

    install(Logging) {
        logger = object : Logger {
            private val _logger = KotlinLogging.logger("CometClient_Ktor")
            override fun log(message: String) {
                _logger.debug(message)
            }
        }
        level = LogLevel.HEADERS
    }
}

val cometClient: CometClient = CometClient()

val coreUpTimer = Timer()
