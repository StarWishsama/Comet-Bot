package ren.natsuyuk1.comet.utils.brotli4j

import com.aayushatharva.brotli4j.Brotli4jLoader
import com.aayushatharva.brotli4j.decoder.BrotliInputStream
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.*
import io.ktor.client.features.compression.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import ren.natsuyuk1.comet.utils.file.resolveDirectory
import ren.natsuyuk1.comet.utils.file.touch
import ren.natsuyuk1.comet.utils.ktor.downloadFile
import ren.natsuyuk1.comet.utils.systeminfo.OsArch
import ren.natsuyuk1.comet.utils.systeminfo.OsType
import ren.natsuyuk1.comet.utils.systeminfo.RuntimeUtil
import java.io.File
import java.nio.file.Path
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import kotlin.io.path.outputStream

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
    private val client = HttpClient(CIO) {
        install(UserAgent) {
            agent =
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/102.0.5005.124 Safari/537.36 Edg/102.0.1245.41"
        }

        install(ContentEncoding) {
            gzip()
            deflate()
            identity()
        }

        install(JsonFeature) {
            serializer = KotlinxSerializer(kotlinx.serialization.json.Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }
    }

    private val brotliLibFolder = File(resolveDirectory("/modules"), "/brotli")

    suspend fun loadBrotli() {
        val libraryName = System.mapLibraryName("brotli")
        val libraryPath = System.getProperty("java.library.path")

        if (libraryPath == null) {
            logger.warn { "无法获取系统的 `java.library.path`, 请检查系统环境变量是否有误!" }
            return
        }

        val libraryLocation = File(libraryPath.split(";")[0], libraryName)

        if (!libraryLocation.exists()) {
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

            val downloadURL =
                "https://repo1.maven.org/maven2/com/aayushatharva/brotli4j/native-$packageName/1.7.1/native-$packageName-1.7.1.jar"
            val downloadFile = File(brotliLibFolder, "native-$packageName-1.7.1.jar")

            kotlin.runCatching {
                downloadFile.touch()
                client.downloadFile(downloadURL, downloadFile)
            }.onSuccess {
                val zip = withContext(Dispatchers.IO) {
                    ZipFile(downloadFile)
                }

                suspend fun copyEntryTo(entry: ZipEntry, output: Path) {
                    withContext(Dispatchers.IO) {
                        zip.getInputStream(entry).use { input ->
                            output.outputStream().use(input::copyTo)
                        }
                    }
                }

                val dll = zip.getEntry("lib/$packageName/$libraryName")

                if (dll == null) {
                    logger.warn { "Brotli 库下载时出现问题, 请手动下载. /lib/$packageName/${libraryName}" }
                    return
                }

                copyEntryTo(dll, libraryLocation.toPath())

                zip.close()
            }.onFailure {
                logger.warn(it) { "Brotli 库下载时出现问题, 请手动下载." }
                return
            }
        }
    }
}
