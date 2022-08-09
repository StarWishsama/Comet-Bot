package ren.natsuyuk1.comet.utils.ktor

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ren.natsuyuk1.comet.utils.file.absPath
import java.io.File
import java.io.InputStream

private val logger = mu.KotlinLogging.logger("CometClient")

suspend fun HttpClient.downloadFile(url: String, file: File) {
    val resp: InputStream = get(url)

    withContext(Dispatchers.IO) {
        resp.use { input ->
            file.outputStream().use { output ->
                input.copyTo(output)
            }
        }
    }

    logger.debug { "Downloaded file ${file.absPath} from $url" }
}
