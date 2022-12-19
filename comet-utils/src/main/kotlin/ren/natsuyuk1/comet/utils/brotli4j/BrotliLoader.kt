package ren.natsuyuk1.comet.utils.brotli4j

import com.aayushatharva.brotli4j.Brotli4jLoader
import com.aayushatharva.brotli4j.decoder.BrotliInputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import ren.natsuyuk1.comet.utils.file.absPath
import ren.natsuyuk1.comet.utils.file.resolveDirectory
import ren.natsuyuk1.comet.utils.file.touch
import ren.natsuyuk1.comet.utils.ktor.defaultClient
import ren.natsuyuk1.comet.utils.ktor.downloadFile
import ren.natsuyuk1.comet.utils.system.OsArch
import ren.natsuyuk1.comet.utils.system.OsType
import ren.natsuyuk1.comet.utils.system.RuntimeUtil
import java.io.File
import java.util.zip.ZipFile

private val logger = KotlinLogging.logger {}

object BrotliDecompressor {
    private var enabled = false

    init {
        try {
            Brotli4jLoader.ensureAvailability()
            enabled = true
        } catch (t: Throwable) {
            logger.warn(t) { "加载 Brotli 库失败, Brotli 功能将不可用." }
        }
    }

    fun isUsable() = enabled

    suspend fun decompress(data: ByteArray): ByteArray = withContext(Dispatchers.Default) {
        if (!isUsable()) error("Brotli library isn't enabled!")

        BrotliInputStream(data.inputStream()).buffered().use {
            it.readAllBytes()
        }
    }
}

object BrotliLoader {
    private val brotliLibFolder = File(resolveDirectory("/modules"), "/brotli")

    suspend fun loadBrotli() {
        val libraryName = System.mapLibraryName("brotli")
        val libraryFile = File(brotliLibFolder, libraryName)

        if (!libraryFile.exists()) {
            val osType = RuntimeUtil.getOsType()
            val osArch = RuntimeUtil.getOsArch()

            val packageName = when (osType) {
                OsType.WINDOWS -> {
                    if (osArch == OsArch.ARM64) {
                        logger.warn { "检测到不受支持的系统/架构, Brotli 功能将被禁用." }
                        return
                    } else {
                        "windows-x86_64"
                    }
                }

                OsType.MACOS -> {
                    if (osArch == OsArch.ARM64) {
                        logger.warn { "检测到不受支持的系统/架构, Brotli 功能将被禁用." }
                        return
                    } else {
                        "osx-x86_64"
                    }
                }

                OsType.LINUX -> {
                    if (osArch == OsArch.ARM64) {
                        "linux-aarch64"
                    } else {
                        "linux-x86_64"
                    }
                }

                else -> {
                    logger.warn { "检测到不受支持的系统/架构, Brotli 功能将被禁用." }
                    return
                }
            }

            /* ktlint-disable max-line-length */
            val downloadURL =
                "https://repo1.maven.org/maven2/com/aayushatharva/brotli4j/native-$packageName/1.8.0/native-$packageName-1.8.0.jar"
            /* ktlint-enable max-line-length */
            val downloadFile = File(brotliLibFolder, "native-$packageName-1.8.0.jar")

            kotlin.runCatching {
                downloadFile.touch()
                defaultClient.downloadFile(downloadURL, downloadFile)
            }.onSuccess {
                val zip = withContext(Dispatchers.IO) {
                    ZipFile(downloadFile)
                }

                val dll = zip.getEntry("lib/$packageName/$libraryName")

                if (dll == null) {
                    logger.warn { "Brotli 库下载时出现问题, 请手动下载. /lib/$packageName/$libraryName" }
                    downloadFile.delete()
                    return
                }

                withContext(Dispatchers.IO) {
                    zip.getInputStream(dll).use { input ->
                        libraryFile.touch()
                        libraryFile.outputStream().use(input::copyTo)
                    }
                }

                logger.info { "成功下载 Brotli 库." }

                zip.close()
                downloadFile.delete()

                System.load(libraryFile.absPath)
            }.onFailure {
                logger.warn(it) { "Brotli 库下载时出现问题, 请手动下载." }
                downloadFile.delete()
                return
            }
        }
    }
}
