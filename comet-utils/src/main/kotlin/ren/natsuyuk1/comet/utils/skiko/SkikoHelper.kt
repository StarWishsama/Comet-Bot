package ren.natsuyuk1.comet.utils.skiko

import io.ktor.client.plugins.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runInterruptible
import mu.KotlinLogging
import org.jetbrains.skiko.Library
import org.jetbrains.skiko.hostId
import ren.natsuyuk1.comet.utils.file.absPath
import ren.natsuyuk1.comet.utils.file.resolveDirectory
import ren.natsuyuk1.comet.utils.ktor.defaultClient
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

object SkikoHelper {
    private const val SKIKO_LIBRARY_PATH_PROPERTY = "skiko.library.path"
    private const val SKIKO_VERSION = "0.7.27"
    private var isLoaded = false

    private val skikoLibraryPath = System.getProperty(SKIKO_LIBRARY_PATH_PROPERTY)

    private val skikoLibFolder = File(resolveDirectory("/modules"), "/skiko")

    private val skikoLib = skikoLibFolder.resolve(System.mapLibraryName("skiko-$hostId"))

    suspend fun findSkikoLibrary() {
        if (skikoLibraryPath != null) {
            return
        }

        skikoLibFolder.mkdirs()

        val skikoOsName = when (RuntimeUtil.getOsType()) {
            OsType.MACOS -> "macos"
            OsType.WINDOWS -> "windows"
            OsType.LINUX -> "linux"
            else -> return
        }

        val skikoArchName = when (RuntimeUtil.getOsArch()) {
            OsArch.X86_64 -> "x64"
            OsArch.ARM64 -> "arm64"
            else -> return
        }

        if (skikoLibFolder.listFiles()?.isEmpty() == true) {
            logger.info { "开始下载 Skiko $SKIKO_VERSION 依赖库." }
            /* ktlint-disable max-line-length */
            val downloadURL = "https://maven.pkg.jetbrains.space/public/p/compose/dev/org/jetbrains/skiko/skiko-awt-runtime-$skikoOsName-$skikoArchName/$SKIKO_VERSION/skiko-awt-runtime-$skikoOsName-$skikoArchName-$SKIKO_VERSION.jar"
            /* ktlint-enable max-line-length */

            kotlin.runCatching {
                val tmpDownloadFile =
                    skikoLibFolder.resolve("skiko-awt-runtime-$skikoOsName-$skikoArchName-$SKIKO_VERSION.jar")
                defaultClient.downloadFile(downloadURL, tmpDownloadFile)
                val zip = runInterruptible {
                    ZipFile(tmpDownloadFile)
                }

                suspend fun copyEntryTo(entry: ZipEntry, output: Path) {
                    runInterruptible(Dispatchers.IO) {
                        zip.getInputStream(entry).use { input ->
                            output.outputStream().use(input::copyTo)
                        }
                    }
                }

                copyEntryTo(
                    zip.getEntry(skikoLib.name) ?: kotlin.run {
                        logger.warn { "下载的 Skiko 文件缺失, 请自行下载." }
                        return
                    },
                    skikoLib.toPath()
                )

                if (skikoOsName == "windows") {
                    val extraEntry = zip.getEntry("icudtl.dat") ?: kotlin.run {
                        logger.warn { "下载的 Skiko 文件缺失, 请自行下载." }
                        return
                    }

                    copyEntryTo(extraEntry, skikoLibFolder.resolve("icudtl.dat").toPath())
                }

                try {
                    zip.close()
                    tmpDownloadFile.delete()
                } catch (e: FileSystemException) {
                    logger.warn("删除缓存 ${tmpDownloadFile.absPath} 失败, 请自行删除.")
                }
            }.onSuccess {
                loadSkikoLibrary()
                FontUtil.loadDefaultFont()
            }.onFailure {
                if (it is ResponseException) {
                    if (it.response.status == HttpStatusCode.NotFound) {
                        logger.warn { "找不到版本为 $SKIKO_VERSION 的 Skiko, 请手动下载." }
                    } else {
                        logger.warn(it) { "在下载 Skiko 库时出现异常, 版本 $SKIKO_VERSION, 如为下载失败请手动下载." }
                    }
                } else {
                    logger.warn(it) { "在下载 Skiko 库时出现异常, 版本 $SKIKO_VERSION, 如为下载失败请手动下载." }
                }
            }
        } else {
            loadSkikoLibrary()
            FontUtil.loadDefaultFont()
        }
    }

    private fun loadSkikoLibrary() {
        try {
            System.setProperty(SKIKO_LIBRARY_PATH_PROPERTY, skikoLibFolder.absPath)
            Library.load()
            isLoaded = true
            logger.info { "成功加载 Skiko $SKIKO_VERSION, Skiko 存放在 ${skikoLibFolder.absPath}" }
        } catch (t: Throwable) {
            logger.warn(t) { "在加载 Skiko 库时出现异常, 版本 $SKIKO_VERSION" }
        }
    }

    fun isSkikoLoaded() = isLoaded
}
