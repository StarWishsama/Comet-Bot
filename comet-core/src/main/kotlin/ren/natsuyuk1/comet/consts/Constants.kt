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
import io.ktor.client.engine.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.compression.*
import io.ktor.client.plugins.cookies.*
import kotlinx.serialization.json.Json
import ren.natsuyuk1.comet.api.config.CometGlobalConfig
import ren.natsuyuk1.comet.network.CometClient
import ren.natsuyuk1.comet.utils.time.Timer
import java.security.cert.X509Certificate
import javax.net.ssl.X509TrustManager
import kotlin.time.Duration.Companion.seconds

val json = Json {
    prettyPrint = true
    isLenient = true
    ignoreUnknownKeys = true
}

val defaultClient = HttpClient(CIO) {
    engine {
        requestTimeout = 0
        val proxyStr = System.getProperty("comet.proxy") ?: System.getenv("COMET_PROXY")
        if (proxyStr.isNullOrBlank()) return@engine

        proxy = ProxyBuilder.http(proxyStr)

        https {
            trustManager = object : X509TrustManager {
                override fun checkClientTrusted(p0: Array<out X509Certificate>?, p1: String?) {}
                override fun checkServerTrusted(p0: Array<out X509Certificate>?, p1: String?) {}
                override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
            }
        }
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

    install(HttpTimeout) {
        requestTimeoutMillis = 30.seconds.inWholeMilliseconds
    }

    install(HttpCookies)
}

val cometClient: CometClient = CometClient()

val cometRunningTimer = Timer()
