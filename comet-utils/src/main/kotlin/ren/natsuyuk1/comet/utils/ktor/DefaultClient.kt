package ren.natsuyuk1.comet.utils.ktor

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.compression.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

internal val defaultClient = HttpClient(CIO) {
    engine {
        requestTimeout = 0
    }

    install(UserAgent) {
        /* ktlint-disable max-line-length */
        agent =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/102.0.5005.124 Safari/537.36 Edg/102.0.1245.41"
        /* ktlint-enable max-line-length */
    }

    install(ContentEncoding) {
        gzip()
        deflate()
        identity()
    }

    install(ContentNegotiation) {
        json(
            Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            }
        )
    }
}
