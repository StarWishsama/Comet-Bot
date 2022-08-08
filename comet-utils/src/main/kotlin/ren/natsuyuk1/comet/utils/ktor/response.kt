package ren.natsuyuk1.comet.utils.ktor

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.utils.io.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

private val logger = mu.KotlinLogging.logger("CometClient")

suspend fun HttpClient.downloadFile(url: String, location: File) {
    this@downloadFile.get<HttpStatement>(url).execute { httpResponse ->
        val channel: ByteReadChannel = httpResponse.receive()

        withContext(Dispatchers.IO) {
            while (!channel.isClosedForRead) {
                val packet = channel.readRemaining(DEFAULT_BUFFER_SIZE.toLong())
                while (!packet.isEmpty) {
                    val bytes = packet.readBytes()
                    location.appendBytes(bytes)
                    logger.debug { "Received ${location.length()} bytes from ${httpResponse.contentLength()}" }
                }
            }
        }
    }
}
