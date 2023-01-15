package ren.natsuyuk1.comet.utils.ktor

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
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

private val downloadingFile = mutableListOf<String>()

fun HttpClientEngineConfig.initProxy() {
    val proxyStr = getEnv("comet.proxy")
    if (proxyStr.isNullOrBlank()) return

    proxy = ProxyBuilder.http(proxyStr)
}

/**
 * 下载链接所对应网页的内容
 *
 * @param url 链接
 * @param file 下载内容储存位置
 * @param verifier 用于验证该响应是否符合要求, 默认仅检查响应码是否为成功
 *
 * @return 下载是否成功
 */
@OptIn(ExperimentalTime::class)
suspend fun HttpClient.downloadFile(
    url: String,
    file: File,
    verifier: (HttpResponse) -> Boolean = { it.status.isSuccess() }
): Boolean {
    if (downloadingFile.contains(url)) {
        return false
    }

    logger.debug { "Trying download file from $url..." }

    var verified = false

    try {
        val duration = measureTime {
            val req = get(url)

            downloadingFile.add(url)

            logger.debug { "Headers = ${req.headers.entries()}" }

            if (verifier(req)) {
                val resp = req.body<InputStream>()

                logger.debug { "Received source size = ${resp.available()}" }

                resp.source().buffer().use { i ->
                    file.sink().buffer().use { o ->
                        o.writeAll(i)
                    }
                }

                verified = true
            }
        }

        if (verified) {
            logger.debug { "Downloaded file ${file.name} from $url, costs $duration" }
        } else {
            logger.debug { "File ${file.name} not be downloaded because extra verify is failed." }
        }
    } catch (e: IOException) {
        logger.warn(e) {}
    } finally {
        downloadingFile.remove(url)
    }

    return verified
}
