package ren.natsuyuk1.comet.utils.ktor

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.*
import io.ktor.client.request.*
import io.ktor.utils.io.core.*
import okio.buffer
import okio.sink
import okio.source
import ren.natsuyuk1.comet.utils.system.getEnv
import java.io.File
import java.io.IOException
import java.io.InputStream
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

private val logger = mu.KotlinLogging.logger("CometClient")

fun HttpClientEngineConfig.initProxy() {
    val proxyStr = getEnv("comet.proxy")
    if (proxyStr.isNullOrBlank()) return

    proxy = ProxyBuilder.http(proxyStr)
}

@OptIn(ExperimentalTime::class)
suspend fun HttpClient.downloadFile(url: String, file: File) {
    try {
        val duration = measureTime {
            val req = get(url)

            logger.debug { "Headers = ${req.headers.entries()}" }

            val resp = req.body<InputStream>()

            resp.source().buffer().use { i ->
                logger.debug { "Received source size = ${i.buffer.size}" }

                file.sink().buffer().use { o ->
                    o.writeAll(i)
                }
            }
        }

        logger.debug { "Downloaded file ${file.name} from $url, costs $duration" }
    } catch (e: IOException) {
        logger.warn(e) {}
    }
}
