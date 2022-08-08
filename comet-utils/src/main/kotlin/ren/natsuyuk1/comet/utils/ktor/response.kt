package ren.natsuyuk1.comet.utils.ktor

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import ren.natsuyuk1.comet.utils.file.writeTextBuffered
import java.io.File

private val logger = mu.KotlinLogging.logger("CometClient")

suspend fun HttpClient.downloadFile(url: String, location: File) {
    val httpResponse: HttpResponse = get(url) {
        onDownload { bytesSentTotal, contentLength ->
            logger.debug { "Received $contentLength bytes from $bytesSentTotal" }
        }
    }
    location.writeTextBuffered(httpResponse.receive())
}
