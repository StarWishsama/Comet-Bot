package ren.natsuyuk1.comet.utils.ktor

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.utils.io.core.*
import okio.buffer
import okio.sink
import okio.source
import ren.natsuyuk1.comet.utils.file.absPath
import java.io.File
import java.io.InputStream

private val logger = mu.KotlinLogging.logger("CometClient")

suspend fun HttpClient.downloadFile(url: String, file: File) {
    val req = get(url)

    logger.debug { "Headers = ${req.headers.entries()}" }

    val resp = req.body<InputStream>()

    resp.source().buffer().use { i ->
        logger.debug { "Received source size = ${i.buffer.size}" }

        file.sink().buffer().use { o ->
            o.writeAll(i)
        }
    }

    logger.debug { "Downloaded file ${file.absPath} from $url" }
}
