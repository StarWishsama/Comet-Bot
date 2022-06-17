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
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import ren.natsuyuk1.comet.api.config.config
import ren.natsuyuk1.comet.network.CometClient

val json = Json {
    prettyPrint = true
    isLenient = true
    ignoreUnknownKeys = true
}

val client: CometClient = CometClient()

val defaultClient = HttpClient(CIO) {
    install(UserAgent) {
        agent = config.useragent
    }

    install(ContentEncoding) {
        gzip()
        deflate()
        identity()
    }

    install(ContentNegotiation) {
        json(json)
    }
}
